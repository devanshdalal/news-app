# Enable requests cache
from importlib import util as importlibutil
if importlibutil.find_spec("requests_cache"):
    import requests_cache
    requests_cache.install_cache('.requests_cache', expire_after=60 * 60 * 12)

from datetime import datetime, timezone
import math
import sys
import os

from pymongo import MongoClient
from newsapi import NewsApiClient

# Constants
PAGE_SIZE = 100
MINUTES_IN_HOUR = 60

# Config
update_interval = 360  # minutes
countries = ['in', 'us', 'gb', 'au', 'ca', 'nz'] # 'in' ['in', 'us', 'gb', 'au', 'ca', 'nz']
categories = {
    'business',
    'entertainment',
    'general',
    'health',
    'science',
    'sports',
    'technology'
}

#Keys
NEWSAPI_ORG_KEY = os.environ.get('NEWSAPI_ORG_KEY')
if not NEWSAPI_ORG_KEY:  # TODO(devansh): Remove
    NEWSAPI_ORG_KEY = 'ea0f26bbe06b44b898f0f0a80af00c7d'
MONGODB_URL = os.environ.get('MONGODB_URL')
if not MONGODB_URL:
    MONGODB_URL = 'mongodb://localhost:27017'

# Init
newsapi = NewsApiClient(api_key=NEWSAPI_ORG_KEY)

def FetchNews(newsapi):
    news = {category: [] for category in categories}
    for category in categories:
        articles = []
        keep_downloading = True
        cindex = 0
        while keep_downloading:
            print('newsapi.get_top_headlines(category=', category,
                  ', page=1', 'country='+countries[cindex] if countries != [] else '', 
                  ', page_size=', PAGE_SIZE, ')')
            r = None
            if countries == []:
                r = newsapi.get_top_headlines(category=category,
                                              page=1,
                                              page_size=PAGE_SIZE)
                if (r['status'] == 'ok'):
                    news[category] = r['articles']
                keep_downloading = False
            else:
                r = newsapi.get_top_headlines(country=countries[cindex],
                                              category=category,
                                              page=1,
                                              page_size=PAGE_SIZE)
                for c in r['articles']:
                    c['country'] = countries[cindex]
                # print(r['articles'])
                news[category].extend(r['articles'])
                cindex += 1
                keep_downloading = cindex < len(countries)

    for category_news in news:
        print(category_news, len(news[category_news]))
    return news

def MakeReadyForImport(data):
    def Transform(category, article):
        article['category'] = category
        return article
    r = []
    for category in categories:
        r.extend(list(map(lambda x: Transform(category, x), data[category])))
    return r


# Init Mongo
mongo_client = MongoClient(MONGODB_URL)
db = mongo_client['feed']
article = db.article

if __name__== "__main__":
    for row in article.find().sort('_id', -1).limit(1):
        diff = datetime.now(timezone.utc) - row['_id'].generation_time
        (m, s) = divmod(diff.total_seconds(), MINUTES_IN_HOUR)
        if (m <= update_interval):
            print('Not fetching/updating, last update:', m, 'minutes ago')
            exit(0)
    news = FetchNews(newsapi)
    news = MakeReadyForImport(news)

    # Drop collection articles
    article.drop()
    article.insert_many(news)

