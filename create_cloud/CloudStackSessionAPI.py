#!/usr/bin/env python

# Author: Will Stevens - wstevens@cloudops.com
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import urllib
import urllib2
import hmac
import hashlib
import base64
import json
import pprint
import time

class CloudStackSessionAPI(object):
    """
    Login and run queries against the Cloudstack API.
    Example Usage: 
    cs_api = CloudstackAPI(username='username', password='password'))
    accounts = cs_api.request(dict({'command':'listAccounts'}))
    
    """
    
    def __init__(self, protocol='http', host='127.0.0.1:8080', uri='/client/api', username=None, password=None, domain=None, logging=True):        
        self.protocol = protocol
        self.host = host
        self.uri = uri
        self.username = username
        self.password = hashlib.md5(password).hexdigest()
        self.sessionkey = None
        self.errors = []
        self.logging = logging
        
        # setup cookie handling
        self.caller = urllib2.build_opener(urllib2.HTTPCookieProcessor())
        urllib2.install_opener(self.caller)
        
        # login so you can get a sessionkey (and a cookie)
        login_query = dict()
        login_query['command'] = 'login'
        login_query['username'] = self.username
        login_query['password'] = self.password
        if domain:
            login_query['domain'] = domain
            
        login_result = self.request(login_query)
        if login_result:
            self.sessionkey = login_result['sessionkey']
        else:
            self.errors.append("Login failed...")
            print self.errors
        
    def request(self, params):
        """Builds a query from params and return a json object of the result or None"""
        if self.sessionkey or (self.username and self.password and params['command'] == 'login'):
            # add the default and dynamic params
            params['response'] = 'json'
            
            if self.sessionkey:
                params['sessionkey'] = self.sessionkey
                print self.sessionkey
            # build the query string
            query_params = map(lambda (k,v):k+"="+urllib.quote(str(v)), params.items())
            query_string = "&".join(query_params)

            # final query string...
            url = self.protocol+"://"+self.host+self.uri+"?"+query_string
            print url
            output = None
            try:
                output = json.loads(self.caller.open(url).read())
            except urllib2.HTTPError, e:
                self.errors.append("HTTPError: "+str(e.code))
            except urllib2.URLError, e:
                self.errors.append("URLError: "+str(e.reason))
               
            if output:
                output = output[(params['command']).lower()+'response']
            
            if self.logging:
                with open('request.log', 'a') as f:
                    f.write('request:\n')
                    f.write(url)
                    f.write('\n\n')
                    f.write('response:\n')
                    if output:
                        pprint.pprint(output, f, 2)
                    else:
                        f.write(repr(self.errors))
                    f.write('\n\n\n\n')
            
            # if the request was an async call, then poll for the result...
            isAsync = False
            while output and 'jobid' in output.keys() and \
                    ('jobstatus' not in output.keys() or ('jobstatus' in output.keys() and output['jobstatus'] == 0)):
                isAsync = True
                print 'polling...'
                time.sleep(2)
                output = self.request(dict({'command':'queryAsyncJobResult', 'jobId':output['jobid']}))
            if(isAsync):
				output = output['jobresult']
            return output
        else:
            self.errors.append("missing credentials in the constructor")
            return None
            
            
if __name__ == "__main__":
    # comment out the following line to keep a history of the requests over multiple runs (request.log will get big).
    open('request.log', 'w').close() # cleans the 'request.log' before execution so it only includes this run.

    host = '10.1.115.41:8080'
    username = 'admin'
    password = 'Shaatir1!'
    domain = None

    cs_api = CloudStackSessionAPI(host=host, username=username, password=password, domain=domain)
    jsonString = cs_api.request(dict({'command':'listAccounts'}))
    print json.dumps(jsonString, sort_keys=True, indent=4, separators=(',', ': '))
