fid = fopen('../bird_queries.blog','w');

% fprintf(fid,'query beta1; \n');
% fprintf(fid,'query beta2; \n');
% fprintf(fid,'query beta3; \n');
% fprintf(fid,'query beta4; \n');

% for t=1:T;
%     fprintf(fid,'query beta(@%d);',t-1);
% end
%fprintf(fid,'\n');
for t=1:T;
    for i=1:GridSize;
        %         fprintf(fid,'query birds(l[%d],@%d);',i-1,t-1);
        %         fprintf(fid,'\n');
        fprintf(fid,'query outflow_vector(l[%d],@%d);',i-1,t-1);
        fprintf(fid,'\n');
    end
end

% fprintf(fid,'query beta1;\n');
% fprintf(fid,'query beta2;\n');
% fprintf(fid,'query beta3;\n');
% fprintf(fid,'query beta4;\n');
fprintf(fid,'query beta(@599);\n');

fclose(fid);