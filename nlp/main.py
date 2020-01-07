#!/usr/bin/python

import sys
import os
from newsapi_utils import FetchNews, GetNewsApiClient, InstallRequestsCache
from db_utils import GetMongoClient, ToUpdateTable, DB
from nlp_utils import TfIdfScores

InstallRequestsCache()

# Init newsapi
newsapi = GetNewsApiClient()

# Init Mongo
mongo_client = GetMongoClient()
db = mongo_client[DB]
article = db.article
preference = db.preference

if not ToUpdateTable(article):
    exit(0)

news = FetchNews(newsapi)

liked_articles = list(preference.find())

news, liked_articles = TfIdfScores(news, liked_articles)

# Drop collection articles
article.drop()
article.insert_many(news)
if len(liked_articles) > 0:
    preference.drop()
    preference.insert_many(liked_articles)
