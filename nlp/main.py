#!/usr/bin/python

import sys
import os
import pickle
from newsapi_utils import FetchNews, GetNewsApiClient, InstallRequestsCache
from db_utils import GetMongoClient, ToUpdateTable, DB
from nlp_utils import TfIdfScores, tf, AddTfIdfWeights

InstallRequestsCache()

# Init newsapi
newsapi = GetNewsApiClient()

# Init Mongo
mongo_client = GetMongoClient()
db = mongo_client[DB]
article = db.article
preference = db.preference
weights = db.weights

# if not ToUpdateTable(article):
#     exit(0)

news = FetchNews(newsapi)

liked_articles = list(preference.find())

news, vectorizer = TfIdfScores(news, tf)

print(news[0])
# Drop collection articles
article.drop()
article.insert_many(news)

# Store pickle obj.
pickle_out = open("tf.pickle","wb")
pickle.dump(vectorizer, pickle_out)
pickle_out.close()


if len(liked_articles) > 0:
    liked = AddTfIdfWeights(liked_articles, vectorizer)
    preference.drop()
    preference.insert_many(liked)

mongo_client.close()
