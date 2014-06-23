function rel_to_prob_plot()

    % For 2 sentence example, plot number of relations vs.
    % P(Rel(SourceFact(Sent[0])) == Rel(SourceFact(Sent[1])))
    
    % Turn off warnings from binomial coefficient
    warning('off', 'all');
    
    % Highest number of relations
    K = 100;
    
    % Fixed sparsity
    beta = 0.1;
    
    % Fixed alpha for Symmetric Dirichlet
    dir_alpha = 0.1;
    
    % Fixed number of entities squared: Currently there are 2 entities
    E = 2;
    
    probs = [];
    for k = 1:K
        
       % Take care of numerator first
       numerator = k * beta * (dir_alpha/(4*dir_alpha + 2));
       sum = 0;
       for n=0:k*E^2 - 1
            sum = sum + nchoosek(k*E^2 - 1, n) * (beta^n * (1 - beta)^(k*E^2 - 1 - n)) * (1/(n+1))^2;
       end
       numerator = numerator * sum;
       
       % Now denominator
       denominator = k * (k-1) * 1/4 * beta^2;
       sum = 0;
       for n=0:k*E^2 - 2
           sum = sum + nchoosek(k*E^2 - 2, n) * (beta^n * (1 - beta)^(k*E^2 - 2 - n)) * (1/(n+2))^2;
       end
       denominator = denominator * sum;
       denominator = denominator + numerator;
       
       probs = [probs numerator/denominator];
       display(['Iteration ' num2str(k) ' complete...']);
        
    end

    plot(probs);
    ylabel('P(Rel(SourceFact(Sent[0])) == Rel(SourceFact(Sent[1])))');
    xlabel('Number of Relations');
    title('2 Sentence Example');
    
    display(['For k = 2, probability is: ' num2str(probs(2))]);
    display(['For k = 3, probability is: ' num2str(probs(3))]);
    display(['For k = 4, probability is: ' num2str(probs(4))]);
    display(['For k = 5, probability is: ' num2str(probs(5))]);
    display(['For k = 10, probability is: ' num2str(probs(10))]);
    display(['For k = 100, probability is: ' num2str(probs(100))]);

end