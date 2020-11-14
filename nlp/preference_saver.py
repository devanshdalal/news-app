#!/usr/bin/python

import sys
import os
import pickle
from nlp_utils import TfIdfScores, AddTfIdfWeights
from db_utils import GetMongoClient, DB
from bson.objectid import ObjectId

mongo_url = sys.argv[1]
preferenceId = ObjectId(sys.argv[2])

pickle_in = open("tf.pickle","rb")
vectorizer = pickle.load(pickle_in)

# 
mongo_client = GetMongoClient(mongo_url)
db = mongo_client[DB]
preference = db.preference

# print(len(news))

liked = preference.find({"_id" : preferenceId})

print('liked', liked)

likedv = AddTfIdfWeights(list(liked), vectorizer)

print('likedv', likedv)

preference.update_one({
  '_id': preferenceId
},{
  '$set': {
    'd.a': likedv[0]['article']['v']
  }
})


