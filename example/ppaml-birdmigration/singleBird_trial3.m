clear all
close all
clc
%%
data=dlmread('data.txt');
features=dlmread('features.txt');

BirdNum=1;
GridSize=4*4;
Year=30; Day=20;
T= Year*Day;
F= Year*(Day-1);

V=zeros(GridSize,GridSize,F);       % allocation

%% Define the model

fid = fopen('blog_trial3.blog','w');

fprintf(fid, '// DBLOG model for birds migration \n');
fprintf(fid,'fixed Real eu = 2.71828182846;  // Eulers constant \n');

fprintf(fid,'random Real beta1 ~ UniformReal(-10.0, 10.0); \n');
fprintf(fid,'random Real beta2 ~ UniformReal(-10.0, 10.0); \n');
fprintf(fid,'random Real beta3 ~ UniformReal(-10.0, 10.0); \n');
fprintf(fid,'random Real beta4 ~ UniformReal(-10.0, 10.0); \n');
fprintf(fid,'\n');

%% define pItoJ_t 's
UpTo=20;
fprintf('creating blog file, pItoJ''s; Progress: ')
reverseStr=[];
counter=0;
Total=UpTo*GridSize^2;

for t=1:UpTo;
    for i=1:GridSize;
        for j=1:GridSize;
            counter=counter+1;
            reverseStr=displayprogress(counter/Total*100,reverseStr);
            
            v =  features( (t-1)*GridSize^2 + (i-1)*GridSize + j, 5:end) ;
            str=strcat(' random Real p',num2str(i),'to',num2str(j),'_',num2str(t),'= eu^(beta1*',num2str(v(1)),' + beta2*',num2str(v(2)),' + beta3*',num2str(v(3)),' + beta4*',num2str(v(4)),');' );
            fprintf(fid,str);
        end
        fprintf(fid,'\n');
    end
end
fprintf(fid,'\n');
fprintf('\n');
%% define the model

fprintf(fid,'type Location; \n');
fprintf(fid,'distinct Location L1, L2, L3, L4, L5, L6, L7, L8, L9, L10, L11, L12, L13, L14, L15, L16; \n');
fprintf(fid,'\n');
fprintf(fid,'type Bird; \n');
str=strcat('distinct Bird Bird[',num2str(BirdNum),']; \n');
fprintf(fid,str);
fprintf(fid,'\n');
fprintf(fid,'random Location loc(Bird b, Timestep t) { \n');
fprintf(fid,'    if t == @0 then ~ ');
fprintf(fid,'    Categorical({ L1->0.05, L2->0.05, L3->0.05, L4->0.05, L5->0.05, L6->0.05, L7->0.05, L8->0.05, L9->0.05, L10->0.05, L11->0.05, L12->0.05, L13->0.05, L14->0.05, L15->0.05, L16->0.05 }) \n');

%% define transitions
counter=0;
reverseStr=[];
fprintf('creating blog file, transitions; Progress: ')

for t=1:UpTo;
    str=strcat('// t == @',num2str(t),' \n');
    fprintf(fid,str);
    str=strcat('   else if t == @',num2str(t),' then\n');
    fprintf(fid,str);
    
    for i=1:GridSize;
        
        if i==1;
            str=strcat('if      loc(b, Prev(t)) == L',num2str(i),' then ~ Categorical({');
            fprintf(fid,str);
        elseif i==GridSize;
            str=strcat('else    ~ Categorical({');
            fprintf(fid,str);
        else
            str=strcat('else if loc(b, Prev(t)) == L',num2str(i),' then ~ Categorical({');
            fprintf(fid,str);
        end
        
        for j=1:GridSize;
            counter=counter+1;
            reverseStr=displayprogress(counter/Total*100,reverseStr);
            
            if j== GridSize;
                str=strcat('L',num2str(j),' -> p',num2str(i),'to',num2str(j),'_',num2str(t));
            else
                str=strcat(' L',num2str(j),' -> p',num2str(i),'to',num2str(j),'_',num2str(t),',');
            end
            fprintf(fid,str);
        end
        
        fprintf(fid,' }) \n');
    end
    
end
fprintf(fid,'// Rest of the time ticks \n');
fprintf(fid,'else ~  Categorical({ L1->0.05, L2->0.05, L3->0.05, L4->0.05, L5->0.05, L6->0.05, L7->0.05, L8->0.05, L9->0.05, L10->0.05, L11->0.05, L12->0.05, L13->0.05, L14->0.05, L15->0.05, L16->0.05 }) \n');
fprintf(fid,'}; \n');
fprintf('\n');

%% Define observation functions
fprintf(fid,'\n');
fprintf(fid,'// the number of birds at locate (s), and time tick t; \n');
fprintf(fid,'random Integer num(Location s, Timestep t) ~ Size({Bird b: loc(b, t) == s}); \n');
fprintf(fid,'random Integer NoisyObs(Location s, Timestep t){ \n');
fprintf(fid,'   if num(s, t) == 0 then = 0 \n');
fprintf(fid,'   else ~ Poisson( num(s, t) ) \n');
fprintf(fid,'}; \n');

%% Supply the inputs
fprintf(fid,'\n');
fprintf('creating blog file, observations; Progress: ')
fprintf(fid,'// Observations\n');

Total=UpTo*GridSize;
counter=0;
reverseStr=[];

for t=1:UpTo;
    for i=1:GridSize;
        counter=counter+1;
        reverseStr=displayprogress(counter/Total*100,reverseStr);
        
        str=strcat('obs NoisyObs(L',num2str(i), ', @',num2str(t-1),') = ',num2str(data(t,2+i)),'; \n');
        fprintf(fid,str);
        
    end
    fprintf(fid,'\n');
    
end
fprintf('\n');

%% Queries
fprintf(fid,'// Queries\n');
query_num=0;
if(query_num==1)
    fprintf('creating blog file, queries; Progress: ')
    
    Total=UpTo*GridSize;
    counter=0;
    reverseStr=[];
    
    for t=1:UpTo;
        for i=1:GridSize;
            counter=counter+1;
            reverseStr=displayprogress(counter/Total*100,reverseStr);
            
            str=strcat('query num(L',num2str(i), ', @',num2str(t-1),'); \n');
            fprintf(fid,str);
            
        end
        fprintf(fid,'\n');
        
    end
    fprintf('\n');
end

fprintf(fid,'query beta1; \n');
fprintf(fid,'query beta2; \n');
fprintf(fid,'query beta3; \n');
fprintf(fid,'query beta4; \n');

%% Stop writing into .blog file
fclose(fid);



