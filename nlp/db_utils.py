#!/usr/bin/python

import os
from datetime import datetime, timezone

from pymongo import MongoClient

# Constants
DB = 'feed'
MINUTES_IN_HOUR = 60

# Config
update_interval = MINUTES_IN_HOUR # minutes

MONGODB_URL = os.environ.get('MONGODB_URL')
if not MONGODB_URL:
    MONGODB_URL = 'mongodb://localhost:27017'

def GetMongoClient():
	print('using MONGODB_URL:', MONGODB_URL)
	return MongoClient(MONGODB_URL)

def ToUpdateTable(table):
	for row in table.find().sort('_id', -1).limit(1):
	    diff = datetime.now(timezone.utc) - row['_id'].generation_time
	    (m, s) = divmod(diff.total_seconds(), MINUTES_IN_HOUR)
	    if (m <= update_interval):
	        print('Not fetching/updating, last update:', m, 'minutes ago')
	        return False
	return True