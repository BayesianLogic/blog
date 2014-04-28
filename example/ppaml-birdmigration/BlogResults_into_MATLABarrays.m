% throw away textual BLOG output
particleFilter=1; % if LWSampler set to 0

if(particleFilter)
    system('tail -n +13 results_from_blog.txt > tmp');
    system('mv tmp results_from_blog.txt');
    
    system('sed ''/beta/d'' results_from_blog.txt > tmp');
    system('mv tmp results_from_blog.txt');
    
    system('sed ''/Done/d'' results_from_blog.txt > tmp');
    system('mv tmp results_from_blog.txt');
else
    system('tail -n +19 results_from_blog.txt > tmp');
    system('mv tmp results_from_blog.txt');
    
    system('sed ''/beta/d'' results_from_blog.txt > tmp');
    system('mv tmp results_from_blog.txt');
    
    system('sed ''/Done/d'' results_from_blog.txt > tmp');
    system('mv tmp results_from_blog.txt');
end
%%
Results= load('results_from_blog.txt');

Samples=Results(:,2);
Weights=Results(:,1);


