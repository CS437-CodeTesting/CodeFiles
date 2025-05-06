import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Orchestrates the word median calculation workflow.
 */
public class WordMedian {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: wordmedian <in> <out>");
            System.exit(1);
        }

        // Step 1: Run the MapReduce job to count word lengths
        WordLengthCountJob jobRunner = new WordLengthCountJob();
        int jobResult = ToolRunner.run(new Configuration(), jobRunner, args);
        if (jobResult != 0) {
            System.err.println("Word length count job failed.");
            System.exit(jobResult);
        }

        // Step 2: Compute the median from the output
        MedianCalculator medianCalculator = new MedianCalculator();
        double median = medianCalculator.computeMedian(args[1], jobRunner.getTotalWords(), jobRunner.getConf());

        System.out.println("The median word length is: " + median);
    }
}

/**
 * Runs the MapReduce job to count word lengths.
 */
class WordLengthCountJob extends Configured implements Tool {

    private long totalWords = 0;

    public long getTotalWords() {
        return totalWords;
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = getConf();
        Job job = Job.getInstance(conf, "word length count");
        job.setJarByClass(WordLengthCountJob.class);

        job.setMapperClass(WordLengthCountMapper.class);
        job.setCombinerClass(WordLengthCountReducer.class);
        job.setReducerClass(WordLengthCountReducer.class);

        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        boolean success = job.waitForCompletion(true);

        if (success) {
            // Use Hadoop's built-in counter for total words processed
            totalWords = job.getCounters().findCounter(TaskCounter.MAP_OUTPUT_RECORDS).getValue();
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Mapper: Emits (word length, 1) for each word.
     */
    public static class WordLengthCountMapper extends Mapper<Object, Text, IntWritable, IntWritable> {
        private static final IntWritable ONE = new IntWritable(1);
        private final IntWritable length = new IntWritable();

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()) {
                String word = itr.nextToken();
                length.set(word.length());
                context.write(length, ONE);
            }
        }
    }

    /**
     * Reducer: Sums counts for each word length.
     */
    public static class WordLengthCountReducer extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
        private final IntWritable result = new IntWritable();

        @Override
        public void reduce(IntWritable key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }
}

/**
 * Reads the output file and computes the median word length.
 */
class MedianCalculator {

    /**
     * Computes the median word length from the output directory.
     *
     * @param outputDir   The output directory of the MapReduce job.
     * @param totalWords  The total number of words processed.
     * @param conf        Hadoop configuration.
     * @return The median word length.
     * @throws IOException If reading the output fails.
     */
    public double computeMedian(String outputDir, long totalWords, Configuration conf) throws IOException {
        // Find the median indices (1-based)
        int medianIndex1 = (int) Math.ceil(totalWords / 2.0);
        int medianIndex2 = (int) Math.floor((totalWords + 1) / 2.0);

        // Read all part files and aggregate counts
        TreeMap<Integer, Integer> lengthCounts = new TreeMap<>();
        FileSystem fs = FileSystem.get(conf);
        Path outputPath = new Path(outputDir);

        RemoteIterator<LocatedFileStatus> fileStatusListIterator = fs.listFiles(outputPath, false);
        while (fileStatusListIterator.hasNext()) {
            LocatedFileStatus fileStatus = fileStatusListIterator.next();
            String name = fileStatus.getPath().getName();
            if (!name.startsWith("part-")) continue;

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(fs.open(fileStatus.getPath()), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(line);
                    if (st.countTokens() < 2) continue;
                    int length = Integer.parseInt(st.nextToken());
                    int count = Integer.parseInt(st.nextToken());
                    lengthCounts.put(length, lengthCounts.getOrDefault(length, 0) + count);
                }
            }
        }

        // Find the median
        int cumulative = 0;
        Integer medianLen1 = null, medianLen2 = null;
        for (var entry : lengthCounts.entrySet()) {
            int length = entry.getKey();
            int count = entry.getValue();
            int prevCumulative = cumulative;
            cumulative += count;

            if (medianLen1 == null && medianIndex1 > prevCumulative && medianIndex1 <= cumulative) {
                medianLen1 = length;
            }
            if (medianLen2 == null && medianIndex2 > prevCumulative && medianIndex2 <= cumulative) {
                medianLen2 = length;
            }
            if (medianLen1 != null && medianLen2 != null) break;
        }

        if (medianLen1 == null || medianLen2 == null) {
            throw new IOException("Could not determine median from output.");
        }

        return (medianLen1 + medianLen2) / 2.0;
    }
}