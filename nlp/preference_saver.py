#!/usr/bin/python

import sys
import os
import pickle
from nlp_utils import TfIdfScores

# country, category, "author", ...title, ...description, ...content
def ParseArgs(args):
	print('args', args)
	assert(len(args) >= 2)
	article = {}
	# article['country'] = args[1]
	# article['category'] = args[2]
	article['author'] = args[1]
	article['content'] = ' '.join(args[2:])
	print('article', article)
	return article

article = ParseArgs(sys.argv)

pickle_in = open("news.pickle","rb")
news = pickle.load(pickle_in)

news, liked_articles = TfIdfScores(news, [{'article': article}])

# print(len(news))

print(' '.join(map(lambda x: str(x), liked_articles[-1]['article']['v'])))