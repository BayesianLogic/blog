""" Grab sentences with specific trigger to create a data set of about size 1000 

	Take the top 50 triggers (in terms of frequency)

	Num Relations: 20 (approximately)

	Triggers:

		appos|->appos->president->prep->of->pobj->|pobj
		appos|->appos->professor->prep->at->pobj->|pobj
		appos|->appos->director->prep->of->pobj->|pobj
		appos|->appos->head->prep->of->pobj->|pobj
		appos|->appos->spokesman->prep->for->pobj->|pobj
		appos|->appos->chairman->poss->|poss
		appos|->appos->professor->nn->|nn
		nn|<-nn<-president->appos->|appos
		appos|->appos->chairman->prep->of->pobj->|pobj
		appos|->appos->economist->prep->at->pobj->|pobj
		appos|->appos->president->nn->|nn
		appos|->appos->leader->prep->of->pobj->|pobj
		appos|->appos->spokesman->poss->|poss
		appos|->appos->chairman->nn->|nn
		rcmod|->rcmod->director->prep->of->pobj->|pobj
		appos|->appos->analyst->prep->at->pobj->|pobj
		appos|->appos->executive->prep->of->pobj->|pobj
		nsubj|<-nsubj<-tell->dobj->|dobj
		appos|->appos->director->prep->at->pobj->|pobj
		appos|->appos->president->prep->at->pobj->|pobj
		appos|->appos->analyst->prep->for->pobj->|pobj
		appos|->appos->director->prep->for->pobj->|pobj
		rcmod|->rcmod->chairman->prep->of->pobj->|pobj
		rcmod|->rcmod->own->dobj->|dobj
		nn|<-nn<-office->prep->of->pobj->|pobj
		appos|->appos->secretary->poss->|poss
		appos|->appos->spokeswoman->prep->for->pobj->|pobj
		nsubj|<-nsubj<-defeat->dobj->|dobj
		appos|->appos->strategist->prep->at->pobj->|pobj
		appos|->appos->lawyer->prep->for->pobj->|pobj
		appos|->appos->president->poss->|poss
		appos|->appos->firm->nn->|nn
		appos|->appos->director->nn->|nn
		nsubj|<-nsubj<-say->nsubj->|nsubj
		appos|->appos->spokesman->nn->|nn
		nsubj|<-nsubj<-president->prep->of->pobj->|pobj
		appos|->appos->analyst->prep->with->pobj->|pobj
		dep|->dep->director->prep->of->pobj->|pobj
		appos|->appos->director->poss->|poss
		appos|->appos->firm->prep->in->pobj->|pobj
		appos|->appos->leader->nn->|nn
		appos|->appos->analyst->nn->|nn
		dobj|<-dobj<-succeed->prep->as->pobj->director->prep->of->pobj->|pobj
		dobj|<-dobj<-succeed->prep->as->pobj->chairman->prep->of->pobj->|pobj
		prep|->prep->as->pobj->director->prep->of->pobj->|pobj
		appos|<-appos<-today->appos->chairman->prep->of->pobj->|pobj
		dobj|<-dobj<-replace->prep->as->pobj->chairman->prep->of->pobj->|pobj
		rcmod|->rcmod->resign->prep->as->pobj->director->prep->of->pobj->|pobj
		nsubj|<-nsubj<-say->prep->of->pobj->|pobj
		appos|->appos->economist->rcmod->teach->prep->at->pobj->|pobj

"""

import json

def main():

	f = open('/Users/ChrisXie/school/research/code/sampler/pluieTriples_2013_06_12_3.json', 'r', encoding = 'latin-1')

	sents = json.load(f)
	sents = sents['sentences']

	counts = {}

	for item in sents:
		if item['depPath'] not in counts:
			counts[item['depPath']] = 1
		else:
			counts[item['depPath']] += 1

	# Grab top 50 triggers
	counts = list(counts.items())
	counts.sort(key = lambda x : x[1], reverse=True)
	triggers = list(map(lambda x : x[0], (counts)[:50]))

	output_sentences = []

	for item in sents:
		if item['depPath'] in triggers:
			output_sentences.append(item)

	output_sentences.sort(key = lambda x : x['depPath'])

	output_file = open('data.txt', 'w')
	json.dump(output_sentences, output_file)

	# Stats
	print("Number of sentences: " + str(len(output_sentences)))

if __name__ == '__main__':
	main()