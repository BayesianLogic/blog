fid = fopen('../bird_model.blog','w');

fprintf(fid,'// initial value for the birds \n');
fprintf(fid,'fixed RealMatrix initial_value = [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]; \n');

fprintf(fid,'// number of birds at location loc and timestep t \n');
fprintf(fid,'random Integer birds(Location loc, Timestep t){ \n');
fprintf(fid,'if t%%20==@0 then = toInt(initial_value[loc_to_int(loc)]) \n');
% for t=20:20:T;
%    	fprintf(fid,'    else if t==@%d then = toInt(initial_value[loc_to_int(loc)]) \n',t);
% end
fprintf(fid,'    else = toInt(sum({ inflow(src, loc, Prev(t)) for Location src })) \n');
fprintf(fid,'}; \n');

fprintf(fid,'// the vector of outflow from source(src) to all other locations \n');
fprintf(fid,'random RealMatrix outflow_vector(Location src,Timestep t) ~ Multinomial(birds(src,t), probs(src,t)) ; \n');

fprintf(fid,'// inflow from source(src) to destination(dst) \n');
fprintf(fid,'random Integer inflow(Location src, Location dst, Timestep t) = toInt(outflow_vector(src,t)[loc_to_int(dst)]); \n');

fprintf(fid,'// Noisy Observations defined through Poisson distribution \n');
%fprintf(fid,'random Integer NoisyObs(Location loc, Timestep t) ~ Poisson(birds(loc,t)); \n');
fprintf(fid,'random Integer NoisyObs(Location loc, Timestep t){\n');
fprintf(fid,'	if birds(loc,t) == 0 then ~ Poisson(0.01)\n');
fprintf(fid,'   else ~ Poisson(birds(loc,t))\n');
fprintf(fid,'};\n');

fclose(fid);