function [] = plotGenericResult(rowOfset, columnOfset, yLabel, appType, calculatePercentage)
    % Retrieve configuration values
    folderPath = getConfiguration(1);
    numOfSimulations = getConfiguration(2);
    startOfMobileDeviceLoop = getConfiguration(3);
    stepOfMobileDeviceLoop = getConfiguration(4);
    endOfMobileDeviceLoop = getConfiguration(5);
    xTickLabelCoefficient = getConfiguration(6);
    scenarioType = getConfiguration(7);
    legends = getConfiguration(8);
    pos = getConfiguration(9);
    numOfMobileDevices = (endOfMobileDeviceLoop - startOfMobileDeviceLoop) / stepOfMobileDeviceLoop + 1;

    % Initialize result matrices
    all_results = zeros(numOfSimulations, size(scenarioType, 2), numOfMobileDevices);
    min_results = zeros(size(scenarioType, 2), numOfMobileDevices);
    max_results = zeros(size(scenarioType, 2), numOfMobileDevices);

    % Data collection loop
    for s = 1:numOfSimulations
        for i = 1:size(scenarioType, 2)
            for j = 1:numOfMobileDevices
                try
                    mobileDeviceNumber = startOfMobileDeviceLoop + stepOfMobileDeviceLoop * (j - 1);
                    
                    filePath = strcat(folderPath,'/ite',int2str(s),'/SIMRESULT_TWO_TIER_WITH_EO_',char(scenarioType(i)),'_',int2str(mobileDeviceNumber),'DEVICES_',appType,'_GENERIC.log');

                    disp(['Attempting to read file: ', filePath]); % Debugging
                    readData = dlmread(filePath, ';', rowOfset, 0);
                    value = readData(1, columnOfset);

                    % Percentage calculations
                    if strcmp(calculatePercentage, 'percentage_for_all')
                        readData = dlmread(filePath, ';', 1, 0);
                        totalTask = readData(1, 1) + readData(1, 2);
                        value = (100 * value) / totalTask;
                    elseif strcmp(calculatePercentage, 'percentage_for_completed')
                        readData = dlmread(filePath, ';', 1, 0);
                        totalTask = readData(1, 1);
                        value = (100 * value) / totalTask;
                    elseif strcmp(calculatePercentage, 'percentage_for_failed')
                        readData = dlmread(filePath, ';', 1, 0);
                        totalTask = readData(1, 2);
                        value = (100 * value) / totalTask;
                    end

                    all_results(s, i, j) = value;
                catch err
                    warning('Error: %s\nError message: %s', err.message);
                end
            end
        end
    end

    % Average results across simulations
    if numOfSimulations == 1
        results = all_results;
    else
        results = mean(all_results, 1);
    end
    results = squeeze(results); % Remove singleton dimensions

    % Calculate confidence intervals
    for i = 1:size(scenarioType, 2)
        for j = 1:numOfMobileDevices
            x = all_results(:, i, j); % Data for confidence interval calculation
            SEM = std(x) / sqrt(length(x)); % Standard Error
            ts = tinv([0.05 0.95], length(x) - 1); % T-Score
            CI = mean(x) + ts * SEM; % Confidence Interval
            CI(CI < 0) = 0; % Ensure no negative confidence interval values

            min_results(i, j) = results(i, j) - CI(1);
            max_results(i, j) = CI(2) - results(i, j);
        end
    end

    % Plot setup
    types = startOfMobileDeviceLoop:stepOfMobileDeviceLoop:endOfMobileDeviceLoop;
    hFig = figure;
    set(hFig, 'Units', 'centimeters', 'Position', pos);
    hold on;

    % Plotting the results
    if getConfiguration(20) == 1
        markers = getConfiguration(50);
        for j = 1:size(scenarioType, 2)
            if getConfiguration(12) == 1
                errorbar(types, results(j, :), min_results(j, :), max_results(j, :), char(markers(j)), ...
                    'MarkerFaceColor', getConfiguration(20 + j), 'Color', getConfiguration(20 + j), 'LineWidth', 1.5);
            else
                plot(types, results(j, :), char(markers(j)), ...
                    'MarkerFaceColor', getConfiguration(20 + j), 'Color', getConfiguration(20 + j), 'LineWidth', 1.5);
            end
        end
    else
        markers = getConfiguration(40);
        for j = 1:size(scenarioType, 2)
            plot(types, results(j, :), char(markers(j)), 'MarkerFaceColor', 'w', 'LineWidth', 1.2);
        end
    end

    % Finalizing the plot
    lgnd = legend(legends, 'Location', 'NorthWest');
    xlabel(getConfiguration(10));
    ylabel(yLabel);
    axis square;
    grid on;

    % Save the plot if configured
    if ~isempty(get(gca, 'Children')) && getConfiguration(11) == 1
        set(hFig, 'PaperUnits', 'centimeters');
        set(hFig, 'PaperPositionMode', 'manual');
        set(hFig, 'PaperPosition', [0 0 pos(3) pos(4)]);
        set(hFig, 'PaperSize', [pos(3) pos(4)]);
        filename = strcat(folderPath, '\', int2str(rowOfset), '_', int2str(columnOfset), '_', appType);
        saveas(gcf, filename, 'pdf');
    else
        warning('No content in figure. Skipping save operation.');
    end

    hold off;
end