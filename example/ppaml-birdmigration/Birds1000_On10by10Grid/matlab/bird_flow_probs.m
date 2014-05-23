fid = fopen('../bird_flow_probs.blog','w');

fprintf(fid,'// parameters \n');
fprintf(fid,'random Real beta1 ~ UniformReal(4, 12); \n');
fprintf(fid,'random Real beta2 ~ UniformReal(4, 12); \n');
fprintf(fid,'random Real beta3 ~ UniformReal(4, 12); \n');
fprintf(fid,'random Real beta4 ~ UniformReal(4, 12); \n');
fprintf(fid,'random RealMatrix beta(Timestep t) = transpose( __SCALAR_STACK(beta1,beta2,beta3,beta4) ); \n');
fprintf(fid,'// features \n');

fprintf(fid,'random RealMatrix F1(Location src) ~ UniformVector(');
for i=1:GridSize;
    if(i ~= GridSize)
        fprintf(fid,'%f,%f,',-2.5,2.5);
    else
        fprintf(fid,'%f,%f); // feature',-2.5,2.5);
    end
end
fprintf(fid,'\n');
fprintf(fid,'random RealMatrix F2(Location src) ~ UniformVector(');
for i=1:GridSize;
    if(i ~= GridSize)
        fprintf(fid,'%f,%f,',-2.5,2.5);
    else
        fprintf(fid,'%f,%f); // feature',-2.5,2.5);
    end
end
fprintf(fid,'\n');
fprintf(fid,'random RealMatrix F3(Location src, Timestep t) ~ UniformVector(');
for i=1:GridSize;
    if(i ~= GridSize)
        fprintf(fid,'%f,%f,',-2.5,2.5);
    else
        fprintf(fid,'%f,%f); // feature',-2.5,2.5);
    end
end
fprintf(fid,'\n');
fprintf(fid,'random RealMatrix F4(Location src) ~ UniformVector(');
for i=1:GridSize;
    if(i ~= GridSize)
        fprintf(fid,'%f,%f,',-2.5,2.5);
    else
        fprintf(fid,'%f,%f); // feature',-2.5,2.5);
    end
end
fprintf(fid,'\n');
fprintf(fid,'// flow probabilities \n');
fprintf(fid,'random RealMatrix probs(Location src, Timestep t) = exp(beta(t) * vstack(vstack(vstack(transpose(F1(src)),transpose(F2(src))),transpose(F3(src,t))),transpose(F4(src))) ); \n');

fclose(fid);