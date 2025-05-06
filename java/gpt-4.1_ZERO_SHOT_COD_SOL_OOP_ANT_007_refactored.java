public class SystemDiagnosticsDtoFactory {

    public SystemDiagnosticsDTO createSystemDiagnosticsDto(final SystemDiagnostics sysDiagnostics) {
        final SystemDiagnosticsDTO dto = new SystemDiagnosticsDTO();
        final SystemDiagnosticsSnapshotDTO snapshot = new SystemDiagnosticsSnapshotDTO();
        dto.setAggregateSnapshot(snapshot);

        snapshot.setStatsLastRefreshed(new Date(sysDiagnostics.getCreationTimestamp()));

        mapProcessorInfo(snapshot, sysDiagnostics);
        mapThreadInfo(snapshot, sysDiagnostics);
        mapHeapInfo(snapshot, sysDiagnostics);
        mapNonHeapInfo(snapshot, sysDiagnostics);
        mapFlowFileRepositoryStorage(snapshot, sysDiagnostics);
        mapContentRepositoryStorage(snapshot, sysDiagnostics);
        mapProvenanceRepositoryStorage(snapshot, sysDiagnostics);
        mapGarbageCollection(snapshot, sysDiagnostics);
        mapVersionInfo(snapshot);
        mapUptime(snapshot, sysDiagnostics);

        return dto;
    }

    private void mapProcessorInfo(SystemDiagnosticsSnapshotDTO snapshot, SystemDiagnostics sysDiagnostics) {
        snapshot.setAvailableProcessors(sysDiagnostics.getAvailableProcessors());
        snapshot.setProcessorLoadAverage(sysDiagnostics.getProcessorLoadAverage());
    }

    private void mapThreadInfo(SystemDiagnosticsSnapshotDTO snapshot, SystemDiagnostics sysDiagnostics) {
        snapshot.setDaemonThreads(sysDiagnostics.getDaemonThreads());
        snapshot.setTotalThreads(sysDiagnostics.getTotalThreads());
    }

    private void mapHeapInfo(SystemDiagnosticsSnapshotDTO snapshot, SystemDiagnostics sysDiagnostics) {
        snapshot.setMaxHeap(FormatUtils.formatDataSize(sysDiagnostics.getMaxHeap()));
        snapshot.setMaxHeapBytes(sysDiagnostics.getMaxHeap());
        snapshot.setTotalHeap(FormatUtils.formatDataSize(sysDiagnostics.getTotalHeap()));
        snapshot.setTotalHeapBytes(sysDiagnostics.getTotalHeap());
        snapshot.setUsedHeap(FormatUtils.formatDataSize(sysDiagnostics.getUsedHeap()));
        snapshot.setUsedHeapBytes(sysDiagnostics.getUsedHeap());
        snapshot.setFreeHeap(FormatUtils.formatDataSize(sysDiagnostics.getFreeHeap()));
        snapshot.setFreeHeapBytes(sysDiagnostics.getFreeHeap());
        if (sysDiagnostics.getHeapUtilization() != -1) {
            snapshot.setHeapUtilization(FormatUtils.formatUtilization(sysDiagnostics.getHeapUtilization()));
        }
    }

    private void mapNonHeapInfo(SystemDiagnosticsSnapshotDTO snapshot, SystemDiagnostics sysDiagnostics) {
        snapshot.setMaxNonHeap(FormatUtils.formatDataSize(sysDiagnostics.getMaxNonHeap()));
        snapshot.setMaxNonHeapBytes(sysDiagnostics.getMaxNonHeap());
        snapshot.setTotalNonHeap(FormatUtils.formatDataSize(sysDiagnostics.getTotalNonHeap()));
        snapshot.setTotalNonHeapBytes(sysDiagnostics.getTotalNonHeap());
        snapshot.setUsedNonHeap(FormatUtils.formatDataSize(sysDiagnostics.getUsedNonHeap()));
        snapshot.setUsedNonHeapBytes(sysDiagnostics.getUsedNonHeap());
        snapshot.setFreeNonHeap(FormatUtils.formatDataSize(sysDiagnostics.getFreeNonHeap()));
        snapshot.setFreeNonHeapBytes(sysDiagnostics.getFreeNonHeap());
        if (sysDiagnostics.getNonHeapUtilization() != -1) {
            snapshot.setNonHeapUtilization(FormatUtils.formatUtilization(sysDiagnostics.getNonHeapUtilization()));
        }
    }

    private void mapFlowFileRepositoryStorage(SystemDiagnosticsSnapshotDTO snapshot, SystemDiagnostics sysDiagnostics) {
        SystemDiagnosticsSnapshotDTO.StorageUsageDTO flowFileRepoDto =
                createStorageUsageDTO(null, sysDiagnostics.getFlowFileRepositoryStorageUsage());
        snapshot.setFlowFileRepositoryStorageUsage(flowFileRepoDto);
    }

    private void mapContentRepositoryStorage(SystemDiagnosticsSnapshotDTO snapshot, SystemDiagnostics sysDiagnostics) {
        Set<SystemDiagnosticsSnapshotDTO.StorageUsageDTO> contentRepoDtos = new LinkedHashSet<>();
        for (Map.Entry<String, StorageUsage> entry : sysDiagnostics.getContentRepositoryStorageUsage().entrySet()) {
            contentRepoDtos.add(createStorageUsageDTO(entry.getKey(), entry.getValue()));
        }
        snapshot.setContentRepositoryStorageUsage(contentRepoDtos);
    }

    private void mapProvenanceRepositoryStorage(SystemDiagnosticsSnapshotDTO snapshot, SystemDiagnostics sysDiagnostics) {
        Set<SystemDiagnosticsSnapshotDTO.StorageUsageDTO> provenanceRepoDtos = new LinkedHashSet<>();
        for (Map.Entry<String, StorageUsage> entry : sysDiagnostics.getProvenanceRepositoryStorageUsage().entrySet()) {
            provenanceRepoDtos.add(createStorageUsageDTO(entry.getKey(), entry.getValue()));
        }
        snapshot.setProvenanceRepositoryStorageUsage(provenanceRepoDtos);
    }

    private void mapGarbageCollection(SystemDiagnosticsSnapshotDTO snapshot, SystemDiagnostics sysDiagnostics) {
        Set<SystemDiagnosticsSnapshotDTO.GarbageCollectionDTO> gcDtos = new LinkedHashSet<>();
        for (Map.Entry<String, GarbageCollection> entry : sysDiagnostics.getGarbageCollection().entrySet()) {
            gcDtos.add(createGarbageCollectionDTO(entry.getKey(), entry.getValue()));
        }
        snapshot.setGarbageCollection(gcDtos);
    }

    private void mapVersionInfo(SystemDiagnosticsSnapshotDTO snapshot) {
        SystemDiagnosticsSnapshotDTO.VersionInfoDTO versionInfoDto = createVersionInfoDTO();
        snapshot.setVersionInfo(versionInfoDto);
    }

    private void mapUptime(SystemDiagnosticsSnapshotDTO snapshot, SystemDiagnostics sysDiagnostics) {
        snapshot.setUptime(FormatUtils.formatHoursMinutesSeconds(sysDiagnostics.getUptime(), TimeUnit.MILLISECONDS));
    }

    // Helper methods for DTO creation (assumed to exist or to be implemented elsewhere)
    private SystemDiagnosticsSnapshotDTO.StorageUsageDTO createStorageUsageDTO(String id, StorageUsage usage) {
        // Implementation goes here
        // ...
        return new SystemDiagnosticsSnapshotDTO.StorageUsageDTO();
    }

    private SystemDiagnosticsSnapshotDTO.GarbageCollectionDTO createGarbageCollectionDTO(String name, GarbageCollection gc) {
        // Implementation goes here
        // ...
        return new SystemDiagnosticsSnapshotDTO.GarbageCollectionDTO();
    }

    private SystemDiagnosticsSnapshotDTO.VersionInfoDTO createVersionInfoDTO() {
        // Implementation goes here
        // ...
        return new SystemDiagnosticsSnapshotDTO.VersionInfoDTO();
    }
}