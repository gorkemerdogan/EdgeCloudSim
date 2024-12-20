function [] = plotAvgNetworkDelay()

    plotGenericResult(1, 7, 'Average Network Delay (ms)', 'ALL_APPS', '');
    plotGenericResult(1, 7, {'Average Network Delay';'for Image Processing App (ms)'}, 'IMAGE_PROCESSING', '');
    plotGenericResult(1, 7, 'Average Network Delay for Video Processing App (ms)', 'VIDEO_PROCESSING', '');

    % plotGenericResult(5, 1, 'Average WLAN Delay (ms)', 'ALL_APPS', '');
    % plotGenericResult(5, 1, {'Average WLAN Delay';'for Image Processing App (ms)'}, 'IMAGE_PROCESSING', '');
    % plotGenericResult(5, 1, 'Average WLAN Delay for Video Processing App (ms)', 'VIDEO_PROCESSING', '');

    plotGenericResult(5, 2, 'Average LAN Delay (ms)', 'ALL_APPS', '');
    plotGenericResult(5, 2, {'Average LAN Delay';'for Image Processing App (ms)'}, 'IMAGE_PROCESSING', '');
    plotGenericResult(5, 2, 'Average LAN Delay for Video Processing App (ms)', 'VIDEO_PROCESSING', '');

    plotGenericResult(5, 3, 'Average Wi-Fi Delay (ms)', 'ALL_APPS', '');
    plotGenericResult(5, 3, {'Average Wi-Fi Delay';'for Image Processing App (ms)'}, 'IMAGE_PROCESSING', '');
    plotGenericResult(5, 3, 'Average Wi-Fi Delay for Video Processing App (ms)', 'VIDEO_PROCESSING', '');
    
end