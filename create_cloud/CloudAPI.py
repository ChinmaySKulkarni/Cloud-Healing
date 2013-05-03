#!/usr/bin/python
from CloudStackSessionAPI import CloudStackSessionAPI
import json

class CloudAPI(object):
	
	def __init__(self, host='10.1.115.41:8080', username='admin', password='password', domain=None):
		self.host = host
		self.username = username
		self.password = password
		self.domain = domain
		self.cs_key_api = CloudStackSessionAPI(host = self.host, username = self.username, password = self.password, domain = self.domain)

	def execute(self, command, params=None):
		if(params == None):
			params = dict()
		params['command'] = command
		outputJSON = self.cs_key_api.request(params)
		return outputJSON
	
if __name__ == "__main__":
	my_api = CloudAPI(host = '192.168.1.101:8080', password='Shaatir1!')
	returnJSON = my_api.execute('listAccounts')
	print json.dumps(returnJSON, sort_keys=True, indent=4, separators=(',', ': '))
