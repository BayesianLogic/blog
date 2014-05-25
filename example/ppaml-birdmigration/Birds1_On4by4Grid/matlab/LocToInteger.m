fid = fopen('../LocToInteger.blog','w');

fprintf(fid,'// defining the locations \n');
fprintf(fid,'type Location; \n');
fprintf(fid,'distinct Location l[%d]; \n',GridSize);
fprintf(fid,'// the map from loc to integer \n');
fprintf(fid,'random Integer loc_to_int(Location loc) { \n');
for i=1:GridSize;
   if(i==1)
       fprintf(fid,'    if loc == l[%d] then = %d \n',i-1,i-1);
   elseif(i<=GridSize)
       fprintf(fid,'    else if loc == l[%d] then = %d \n',i-1,i-1);
   else
       fprintf(fid,'    else = -1\n');
   end
    
end
fprintf(fid,'};\n');
fclose(fid);
