public class ReportPreviewActionRunner {

    public void run(IAction action) {
        if (!preGenerate()) {
            return;
        }

        IFile file = getSelectedFile();
        if (file == null) {
            disableAction(action);
            return;
        }

        String url = getFileUrl(file);
        Map<String, Object> options = buildOptions(file);

        ReportPreviewer previewer = createPreviewer(action, url, options);
        previewer.preview();
    }

    // --- Helper Methods ---

    private boolean preGenerate() {
        // Existing logic or delegate as needed
        // Placeholder for actual implementation
        return true;
    }

    private IFile getSelectedFile() {
        // Existing logic or delegate as needed
        // Placeholder for actual implementation
        return null;
    }

    private void disableAction(IAction action) {
        action.setEnabled(false);
    }

    private String getFileUrl(IFile file) {
        return file.getLocation().toOSString();
    }

    private Map<String, Object> buildOptions(IFile file) {
        Map<String, Object> options = new HashMap<>();
        options.put(WebViewer.RESOURCE_FOLDER_KEY,
                ReportPlugin.getDefault().getResourceFolder(file.getProject()));
        options.put(WebViewer.SERVLET_NAME_KEY, WebViewer.VIEWER_DOCUMENT);
        return options;
    }

    private ReportPreviewer createPreviewer(IAction action, String url, Map<String, Object> options) {
        Object adapter = ElementAdapterManager.getAdapter(action, IPreviewAction.class);
        if (adapter instanceof IPreviewAction) {
            return new DelegateReportPreviewer((IPreviewAction) adapter, url, options);
        } else {
            return new DefaultReportPreviewer(url, options);
        }
    }

    // --- Polymorphic Previewers ---

    private interface ReportPreviewer {
        void preview();
    }

    private static class DelegateReportPreviewer implements ReportPreviewer {
        private final IPreviewAction delegate;
        private final String url;
        private final Map<String, Object> options;

        DelegateReportPreviewer(IPreviewAction delegate, String url, Map<String, Object> options) {
            this.delegate = delegate;
            this.url = url;
            this.options = options;
        }

        @Override
        public void preview() {
            delegate.setProperty(IPreviewConstants.REPORT_PREVIEW_OPTIONS, options);
            delegate.setProperty(IPreviewConstants.REPORT_FILE_PATH, url);
            delegate.run();
        }
    }

    private static class DefaultReportPreviewer implements ReportPreviewer {
        private final String url;
        private final Map<String, Object> options;

        DefaultReportPreviewer(String url, Map<String, Object> options) {
            this.url = url;
            this.options = options;
        }

        @Override
        public void preview() {
            try {
                WebViewer.display(url, options);
            } catch (Exception e) {
                ExceptionUtil.handle(e);
            }
        }
    }
}