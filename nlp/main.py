#!/usr/bin/python

import sys
import os

MONGODB_URL = os.environ.get('MONGODB_URL')
if not MONGODB_URL:
    MONGODB_URL = 'mongodb://localhost:27017'