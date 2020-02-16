#!/usr/bin/python

import sys
import os
import pickle
from nlp_utils import TfIdfScores

# country, category, "author", ...title, ...description, ...content
def ParseArgs(args):
	assert(len(args) >= 4)
	article = {}
	article['country'] = args[1]
	article['category'] = args[2]
	article['author'] = args[3]
	article['content'] = ' '.join(args[4:])
	return article

article = ParseArgs(sys.argv)

pickle_in = open("news.pickle","rb")
news = pickle.load(pickle_in)

news, liked_articles = TfIdfScores(news, [{'article': article}])

print(' '.join(map(lambda x: str(x), liked_articles[-1]['article']['v'])))