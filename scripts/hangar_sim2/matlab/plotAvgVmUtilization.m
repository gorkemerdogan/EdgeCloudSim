function [] = plotAvgVmUtilization()

    plotGenericResult(2, 8, 'Average VM Utilization (%)', 'ALL_APPS', '');
    plotGenericResult(2, 8, {'Average VM Utilization';'for Image Processing App (%)'}, 'IMAGE_PROCESSING', '');
    plotGenericResult(2, 8, 'Average VM Utilization for Video Processing App (%)', 'VIDEO_PROCESSING', '');

end