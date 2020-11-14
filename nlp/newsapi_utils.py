#!/usr/bin/python

import math
import sys
import os
import re
from datetime import datetime, timezone
from importlib import util as importlibutil

from newsapi import NewsApiClient

# Constants
PAGE_SIZE = 100
MINUTES_IN_HOUR = 60
SECONDS_IN_MINUTE = 60

# Config
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
    NEWSAPI_ORG_KEY = '7bca7fe0b1cf411082fc45d8d78b4dd4'

# Enable requests cache
def InstallRequestsCache():
    if importlibutil.find_spec("requests_cache"):
        print('Enabling requests_cache')
        import requests_cache
        requests_cache.install_cache('.requests_cache', expire_after=SECONDS_IN_MINUTE * MINUTES_IN_HOUR * 12)

def FetchNews(newsapi):
    def Transform(category, article):
        article['category'] = category
        return article
    news = {category: [] for category in categories}
    for category in categories:
        articles = []
        keep_downloading = True
        cindex = 0
        while keep_downloading:
            print('newsapi.get_top_headlines(category=', category,
                  ', page=1', ', country='+countries[cindex] if countries != [] else '', 
                  ', page_size=', PAGE_SIZE, ')')
            r = None
            if countries == []:
                r = newsapi.get_top_headlines(category=category,
                                              page=1,
                                              page_size=PAGE_SIZE,
                                              language='en')
                if (r['status'] == 'ok'):
                    news[category] = r['articles']
                keep_downloading = False
            else:
                r = newsapi.get_top_headlines(country=countries[cindex],
                                              category=category,
                                              page=1,
                                              page_size=PAGE_SIZE,
                                              language='en')
                for c in r['articles']:
                    c['country'] = countries[cindex]
                # print(r['articles'])
                news[category].extend(r['articles'])
                cindex += 1
                keep_downloading = cindex < len(countries)

    for category_news in news:
        print(category_news, len(news[category_news]))
    r = []
    for category in categories:
        r.extend(list(map(lambda x: Transform(category, x), news[category])))
    result = []
    deduper = {}
    for x in r:
        if 'title' not in x or 'url' not in x:
            continue
        if 'description' not in x and 'content' not in x:
            continue
        if x['url'] and x['url'] not in deduper and\
                x['title'] and x['title'] not in deduper and\
                (x['description'] or x['content']):
            deduper[x['url']] = True
            deduper[x['title']] = True
            result.append(x)
    print('len(result)', len(result))
    return result

def GetNewsApiClient():
	return NewsApiClient(api_key=NEWSAPI_ORG_KEY)