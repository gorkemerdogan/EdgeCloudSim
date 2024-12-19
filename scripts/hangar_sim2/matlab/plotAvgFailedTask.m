function [] = plotAvgFailedTask()

    plotGenericResult(1, 2, 'Failed Tasks (%)', 'ALL_APPS', 'percentage_for_all');
    plotGenericResult(1, 2, {'Failed Tasks for';'Image Processing App (%)'}, 'IMAGE_PROCESSING', 'percentage_for_all');
    plotGenericResult(1, 2, 'Failed Tasks for Video Processing App (%)', 'VIDEO_PROCESSING', 'percentage_for_all');

    plotGenericResult(2, 2, 'Failed Tasks on Edge (%)', 'ALL_APPS', 'percentage_for_all');
    plotGenericResult(2, 2, {'Failed Tasks on Edge';'for Image Processing App (%)'}, 'IMAGE_PROCESSING', 'percentage_for_all');
    plotGenericResult(2, 2, 'Failed Tasks on Edge for Video Processing App (%)', 'VIDEO_PROCESSING', 'percentage_for_all');

    plotGenericResult(3, 2, 'Failed Tasks on Cloud (%)', 'ALL_APPS', 'percentage_for_all');
    plotGenericResult(3, 2, {'Failed Tasks on Cloud for';'Image Processing App (%)'}, 'IMAGE_PROCESSING', 'percentage_for_all');
    plotGenericResult(3, 2, 'Failed Tasks on Cloud for Video Processing App (%)', 'VIDEO_PROCESSING', 'percentage_for_all');

end