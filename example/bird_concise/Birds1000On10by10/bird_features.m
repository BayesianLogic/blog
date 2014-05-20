clear all
close all
clc
%% Create the features
% f1, f2, f4 features do not change with time, Create a matrix for those
% wind features change with time
data=dlmread('traindata.txt');
features=dlmread('trainfeatures.txt');

BirdNum=1000;
GridSize=10*10;
Year=3; Day=20;
T= Year*Day;
F= Year*(Day-1);

new_features = zeros(T*GridSize^2,8);

for i=1:Year;
    new_features(1 + GridSize^2*Day*(i-1) : GridSize^2*Day*i - GridSize^2,:) = features(1 + GridSize^2*(Day-1)*(i-1) : GridSize^2*(Day-1)*i,:);
    new_features(1 + GridSize^2*Day*i - GridSize^2 : GridSize^2*Day*i, :) = repmat([1 20 0 0 0 0 0 0],GridSize^2,1);
end

fid = fopen('bird_features.blog','w');
%% F1
fprintf(fid,'//feature 1\n');
for i = 1:GridSize;
    fprintf(fid,'obs F1(l[%d]) = __SCALAR_STACK(',i-1);
    for j = 1:GridSize;
        if(j<GridSize)
            fprintf( fid,'%f,',features(j+(i-1)*GridSize,5) );
        else
            fprintf( fid,'%f);',features(j+(i-1)*GridSize,5) );
        end
    end
    fprintf(fid,'\n');
end
fprintf(fid,'\n');
%% F2
fprintf(fid,'//feature 2\n');
for i = 1:GridSize;
    fprintf(fid,'obs F2(l[%d]) = __SCALAR_STACK(',i-1);
    for j = 1:GridSize;
        if(j<GridSize)
            fprintf( fid,'%f,',features(j+(i-1)*GridSize,6) );
        else
            fprintf( fid,'%f);',features(j+(i-1)*GridSize,6) );
        end
    end
    fprintf(fid,'\n');
end
fprintf(fid,'\n');
%% F4
fprintf(fid,'//feature 4\n');
for i = 1:GridSize;
    fprintf(fid,'obs F4(l[%d]) = __SCALAR_STACK(',i-1);
    for j = 1:GridSize;
        if(j<GridSize)
            fprintf( fid,'%f,',features(j+(i-1)*GridSize,8) );
        else
            fprintf( fid,'%f);',features(j+(i-1)*GridSize,8) );
        end
    end
    fprintf(fid,'\n');
end
fprintf(fid,'\n');

%% F3
fprintf(fid,'//feature 3\n');
for t=1:T;
    fprintf(fid,'//Timestep %d\n',t-1);
    for i = 1:GridSize;
        fprintf(fid,'obs F3(l[%d],@%d) = __SCALAR_STACK(',i-1,t-1);
        for j = 1:GridSize;
            if(j<GridSize)
                fprintf( fid,'%f,',new_features(GridSize^2*(t-1)+j+(i-1)*GridSize,7) );
            else
                fprintf( fid,'%f);',new_features(GridSize^2*(t-1)+j+(i-1)*GridSize,7) );
            end
        end
        fprintf(fid,'\n');
    end
end
fprintf(fid,'\n');

%%
fclose(fid);