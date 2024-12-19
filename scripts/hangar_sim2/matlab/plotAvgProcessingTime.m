function [] = plotAvgProcessingTime()

    plotGenericResult(1, 6, 'Processing Time (sec)', 'ALL_APPS', '');
    plotGenericResult(1, 6, 'Processing Time for Image Processing App (sec)', 'IMAGE_PROCESSING', '');
    plotGenericResult(1, 6, 'Processing Time for Video Processing (sec)', 'VIDEO_PROCESSING', '');

    plotGenericResult(2, 6, 'Processing Time on Edge (sec)', 'ALL_APPS', '');
    plotGenericResult(2, 6, {'Processing Time on Edge';'for Image Processing App (sec)'}, 'IMAGE_PROCESSING', '');
    plotGenericResult(2, 6, {'Processing Time on Edge';'for Video Processing (sec)'}, 'VIDEO_PROCESSING', '');

    plotGenericResult(3, 6, 'Processing Time on Cloud (sec)', 'ALL_APPS', '');
    plotGenericResult(3, 6, {'Processing Time on Cloud';'for Image Processing App (sec)'}, 'IMAGE_PROCESSING', '');
    plotGenericResult(3, 6, {'Processing Time on Cloud';'for Video Processing App (sec)'}, 'VIDEO_PROCESSING', '');

end