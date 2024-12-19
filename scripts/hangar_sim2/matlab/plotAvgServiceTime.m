function [] = plotAvgServiceTime()

    plotGenericResult(1, 5, 'Service Time (sec)', 'ALL_APPS', '');
    plotGenericResult(1, 5, {'Service Time for';'Image Processing App (sec)'}, 'IMAGE_PROCESSING', '');
    plotGenericResult(1, 5, 'Service Time for Video Processing App (sec)', 'VIDEO_PROCESSING', '');

    plotGenericResult(2, 5, 'Service Time on Edge (sec)', 'ALL_APPS', '');
    plotGenericResult(2, 5, {'Service Time on Edge';'for Image Processing App (sec)'}, 'IMAGE_PROCESSING', '');
    plotGenericResult(2, 5, 'Service Time on Edge for Video Processing App (sec)', 'VIDEO_PROCESSING', '');

    plotGenericResult(3, 5, 'Service Time on Cloud (sec)', 'ALL_APPS', '');
    plotGenericResult(3, 5, {'Service Time on Cloud';'for Image Processing App (sec)'}, 'IMAGE_PROCESSING', '');
    plotGenericResult(3, 5, 'Service Time on Cloud for Video Processing App (sec)', 'VIDEO_PROCESSING', '');

end