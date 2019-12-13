# Enable requests cache
from importlib import util as importlibutil
if importlibutil.find_spec("requests_cache"):
    import requests_cache
    requests_cache.install_cache('.requests_cache', expire_after=60 * 60 * 12)

import datetime
import math
import sys

from newsapi import NewsApiClient

# Constants
PAGE_SIZE = 100

# Config
countries = {}

# Init
newsapi = NewsApiClient(api_key='ea0f26bbe06b44b898f0f0a80af00c7d')

# extracted = ExtractSources()
# print('extracted', extracted)
# exit(0)

categories = {
    'business',
    'entertainment',
    'general',
    'health',
    'science',
    'sports',
    'technology'
}

news = {category: {} for category in categories}

for category in categories:
    articles = []
    keep_downloading = True
    cindex = 0
    while keep_downloading:
        print('newsapi.get_top_headlines(category=', category,
              ', page=1'
              ', page_size=', PAGE_SIZE, ')')
        r = None
        if countries == {}:
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
            cindex += 1
            keep_downloading = cindex < len(countries)
            news[category][country] = r['articles']

for category_news in news:
    print(category_news, len(news[category_news]))



