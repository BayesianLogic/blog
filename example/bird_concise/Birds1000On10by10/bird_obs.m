fid = fopen('bird_obs.blog','w');
fprintf(fid,'// Observations\n');

Total=T*GridSize;
counter=0;

for t=1:T;
    for i=1:GridSize;
        
%         str=strcat('obs NoisyObs',num2str(i-1),'(@',num2str(t-1),') = ',num2str(data(t,2+i)),'; \n');
%         fprintf(fid,str);
        
        fprintf(fid,'obs NoisyObs(l[%d],@%d) = %d;\n',i-1,t-1,data(t,2+i));
        
    end
    fprintf(fid,'\n');
    
end
fprintf('\n');

fclose(fid);

