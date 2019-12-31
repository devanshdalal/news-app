#!/usr/bin/python

import re

from sklearn.feature_extraction.text import TfidfVectorizer
from nltk.stem import WordNetLemmatizer
from nltk.stem.porter import PorterStemmer

# Config
max_features = 50  # max features to be used in TfidfVectorizer

def ExtractText(data):
    def Prune(s):
        if not s:
            return ''
        return s
    def StripFromEnd(s):
        return re.sub(r'\[.+\]$', '', Prune(s))
    def Special(pre, text):
        return pre + '_' + text
    def ExtractItem(item):
        return ' '.join([Prune(item['title']), Prune(item['description']), StripFromEnd(item['content']),
                         Special('country', item['country']), Special('category', item['category'])])
    result = [''] * len(data)
    for i, item in enumerate(data):
        result[i] = ExtractItem(item)
    return result

tf = TfidfVectorizer(max_features=max_features, stop_words='english')

def TfIdfScores(news, liked = []):
    extracted_news = ExtractText(news)
    fit_transform = tf.fit_transform(extracted_news)
    # print(fit_transform[0].todense())
    for i,_ in enumerate(news):
        news[i]['v'] = fit_transform[i].todense().tolist()[0]
    
    # print('i', 0, news[0]['v'])

    if len(liked) > 0:
        extracted_liked = ExtractText(liked)

        transform = tf.transform(extracted_liked)
        for i,_ in enumerate(liked):
            liked[i]['v'] = transform[i].todense().tolist()[0]

    return news, liked


