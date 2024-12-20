function [] = plotTaskFailureReason()

    plotGenericResult(1, 10, 'Failed Task due to VM Capacity (%)', 'ALL_APPS', 'percentage_for_failed');
    plotGenericResult(1, 10, {'Failed Task due to VM Capacity';'for Image Processing App (%)'}, 'IMAGE_PROCESSING', 'percentage_for_failed');
    plotGenericResult(1, 10, {'Failed Task due to VM Capacity';'for Video Processing App (%)'}, 'VIDEO_PROCESSING', 'percentage_for_failed');

    % plotGenericResult(1, 11, 'Failed Task due to Mobility (%)', 'ALL_APPS', 'percentage_for_failed');
    % plotGenericResult(1, 11, {'Failed Task due to Mobility';'for Image Processing App (%)'}, 'IMAGE_PROCESSING', 'percentage_for_failed');
    % plotGenericResult(1, 11, {'Failed Task due to Mobility';'for Video Processing App (%)'}, 'VIDEO_PROCESSING', 'percentage_for_failed');

    % plotGenericResult(5, 5, 'Failed Tasks due to WLAN failure (%)', 'ALL_APPS', 'percentage_for_failed');
    % plotGenericResult(5, 5, {'Failed Tasks due to WLAN failure';'for Image Processing App (%)'}, 'IMAGE_PROCESSING', 'percentage_for_failed');
    % plotGenericResult(5, 5, {'Failed Tasks due to WLAN failure';'for Video Processing App (%)'}, 'VIDEO_PROCESSING', 'percentage_for_failed');

    plotGenericResult(5, 6, 'Failed Tasks due to LAN failure (%)', 'ALL_APPS', 'percentage_for_failed');
    plotGenericResult(5, 6, {'Failed Tasks due to LAN failure';'for Image Processing App (%)'}, 'IMAGE_PROCESSING', 'percentage_for_failed');
    plotGenericResult(5, 6, {'Failed Tasks due to LAN failure';'for Video Processing App (%)'}, 'VIDEO_PROCESSING', 'percentage_for_failed');

    plotGenericResult(5, 7, 'Failed Tasks due to Wi-Fi failure (%)', 'ALL_APPS', 'percentage_for_failed');
    plotGenericResult(5, 7, {'Failed Tasks due to Wi-Fi failure';'for Image Processing App (%)'}, 'IMAGE_PROCESSING', 'percentage_for_failed');
    plotGenericResult(5, 7, {'Failed Tasks due to Wi-Fi failure';'for Video Processing App (%)'}, 'VIDEO_PROCESSING', 'percentage_for_failed');

end