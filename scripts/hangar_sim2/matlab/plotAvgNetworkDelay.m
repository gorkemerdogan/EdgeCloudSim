function [] = plotAvgNetworkDelay()

    plotGenericResult(1, 7, 'Average Network Delay (sec)', 'ALL_APPS', '');
    plotGenericResult(1, 7, {'Average Network Delay';'for Image Processing App (sec)'}, 'IMAGE_PROCESSING', '');
    plotGenericResult(1, 7, 'Average Network Delay for Video Processing App (sec)', 'VIDEO_PROCESSING', '');

    plotGenericResult(5, 1, 'Average WLAN Delay (sec)', 'ALL_APPS', '');
    plotGenericResult(5, 1, {'Average WLAN Delay';'for Image Processing App (sec)'}, 'IMAGE_PROCESSING', '');
    plotGenericResult(5, 1, 'Average WLAN Delay for Video Processing App (sec)', 'VIDEO_PROCESSING', '');

    plotGenericResult(5, 2, 'Average MAN Delay (sec)', 'ALL_APPS', '');
    plotGenericResult(5, 2, {'Average MAN Delay';'for Image Processing App (sec)'}, 'IMAGE_PROCESSING', '');
    plotGenericResult(5, 2, 'Average MAN Delay for Video Processing App (sec)', 'VIDEO_PROCESSING', '');

    plotGenericResult(5, 3, 'Average WAN Delay (sec)', 'ALL_APPS', '');
    plotGenericResult(5, 3, {'Average WAN Delay';'for Image Processing App (sec)'}, 'IMAGE_PROCESSING', '');
    plotGenericResult(5, 3, 'Average WAN Delay for Video Processing App (sec)', 'VIDEO_PROCESSING', '');
    
end