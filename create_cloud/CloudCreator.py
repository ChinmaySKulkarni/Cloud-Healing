from CloudAPI import CloudAPI
import time
import hashlib

class CloudCreator:
	def __init__(self):
		open('request.log', 'w').close()
		#self.my_api = CloudAPI(password='Shaatir1!')
		self.my_api = CloudAPI()

		output = self.my_api.execute('listAccounts')
		account = output['account'][0]
		user = account['user'][0]
		user_id = user['id']
		

		print "Changing Password..."		
		params = dict()
		params['id'] = user_id
		params['password'] = hashlib.md5('Shaatir1!').hexdigest()
		self.my_api.execute('updateUser', params)
		
		print "Adding Zone..."		
		params = dict()
		params['dns1'] = '10.1.101.30'
		params['internaldns1'] = '10.1.101.30'
		params['name'] = 'Cloud Healing Zone'
		params['networktype'] = 'Basic'
		zoneJSON = self.my_api.execute('createZone', params)
		zone_id = zoneJSON['zone']['id']

		print "Adding Guest Network..."						
		params = dict()		
		params['zoneid'] = zone_id
		params['name'] = 'PhysicalNetworkInBasicZone'
		physical_netJSON = self.my_api.execute('createPhysicalNetwork', params)
		physical_net_id = physical_netJSON['physicalnetwork']['id']

		print "Adding Guest Traffic..."	
		params = dict()
		params['trafficType'] = 'Guest'
		params['physicalnetworkid'] = physical_net_id
		self.my_api.execute('addTrafficType', params)
		
		print "Adding Management Traffic..."	
		params = dict()
		params['trafficType'] = 'Management'
		params['physicalnetworkid'] = physical_net_id
		self.my_api.execute('addTrafficType', params)
		
		print "Enabling Physical Network..."
		params = dict()
		params['state'] = 'Enabled'
		params['id'] = physical_net_id
		self.my_api.execute('updatePhysicalNetwork', params)

		params = dict()
		params['name'] = 'VirtualRouter'
		params['physicalNetworkId'] = physical_net_id
		net_service_providerJSON = self.my_api.execute('listNetworkServiceProviders', params)		
		net_service_id = net_service_providerJSON['networkserviceprovider'][0]['id']
		
		params = dict()
		params['nspid'] = net_service_id
		virt_routerJSON = self.my_api.execute('listVirtualRouterElements', params)
		virt_router_id = virt_routerJSON['virtualrouterelement'][0]['id']
		
		print "Configuring Virtual Router..."
		params = dict()
		params['enabled'] = 'true'
		params['id'] = virt_router_id
		self.my_api.execute('configureVirtualRouterElement', params)
		
		params = dict()
		params['state'] = 'Enabled'
		params['id'] = net_service_id
		self.my_api.execute('updateNetworkServiceProvider', params)
		
		params = dict()
		params['name'] = 'SecurityGroupProvider'
		params['physicalNetworkId'] = physical_net_id
		sec_grp_provJSON = self.my_api.execute('listNetworkServiceProviders', params)
		sec_grp_id = sec_grp_provJSON['networkserviceprovider'][0]['id']
		
		print "Updating Network Service Provider..."		
		params = dict()
		params['state'] = 'Enabled'
		params['id'] = sec_grp_id
		self.my_api.execute('updateNetworkServiceProvider', params)
		
		params = dict()
		params['state'] = 'Enabled'
		params['guestiptype'] = 'Shared'
		net_offer_listJSON = self.my_api.execute('listNetworkOfferings', params)
		for network_offering in net_offer_listJSON['networkoffering']:
			if(network_offering['name'] == 'DefaultSharedNetworkOfferingWithSGService'):
				net_offer_id = network_offering['id']
				break
			
		print "Creating Network..."
		params = dict()
		params['zoneid'] = zone_id
		params['name'] = 'guestNetworkForBasicZone'
		params['displaytext'] = 'guestNetworkForBasicZone'
		params['networkofferingid'] = net_offer_id
		networkJSON = self.my_api.execute('createNetwork', params)
		network_id = networkJSON['network']['id']
		
		print "Creating Pod..."
		params = dict()
		params['zoneid'] = zone_id
		params['name'] = 'Cloud-Healing-Pod-1'
		params['gateway'] = '10.1.115.1'
		params['netmask'] = '255.255.255.0'
		params['startIp'] = '10.1.115.35'
		params['endIp'] = '10.1.115.43'
		podJSON = self.my_api.execute('createPod', params)
		pod_id = podJSON['pod']['id']
		
		print "Creating VLAN..."		
		params = dict()
		params['podid'] = pod_id
		params['networkid'] = network_id
		params['gateway'] = '10.1.115.1'
		params['netmask'] = '255.255.255.0'
		params['startip'] = '10.1.115.28'
		params['endip'] = '10.1.115.34'
		params['forVirtualNetwork'] = 'false'
		self.my_api.execute('createVlanIpRange', params)
		
		print "Creating Cluster..."		
		params = dict()
		params['zoneId'] = zone_id
		params['hypervisor'] = 'KVM'
		params['clustertype'] = 'CloudManaged'
		params['podId'] = pod_id
		params['clustername'] = 'Cloud-Healing-Cluster-1'
		clusterJSON = self.my_api.execute('addCluster', params)
		cluster_id = clusterJSON['cluster'][0]['id']
		
		
		print "Adding Host..."
		params = dict()
		params['zoneid'] = zone_id
		params['podid'] = pod_id
		params['clusterid'] = cluster_id
		params['hypervisor'] = 'KVM'
		params['clustertype'] = 'CloudManaged'
		params['hosttags'] = 'undefined'
		params['username'] = 'root'
		params['password'] = 'Shaatir1!'
		params['url'] = 'http://10.1.115.42'
		self.my_api.execute('addHost', params)

		print "Creating Storage Pool..."
		params = dict()
		params['zoneid'] = zone_id
		params['podId'] = pod_id
		params['clusterid'] = cluster_id
		params['name'] = 'Cloud-Healing-Primary-Storage-1'
		params['url'] = 'nfs://10.1.115.41/export/primary'
		self.my_api.execute('createStoragePool', params)
		
		print "Adding Secondary Storage..."
		params = dict()
		params['zoneId'] = zone_id
		params['url'] = 'nfs://10.1.115.41/export/secondary'
		self.my_api.execute('addSecondaryStorage', params)
		
		print "Updating Zone..."
		params = dict()
		params['allocationstate'] = 'Enabled'
		params['id'] = zone_id
		self.my_api.execute('updateZone', params)
		
		print "Waiting for SystemVMs to get Ready..."
		keepRunning = True
		while(keepRunning):
			params = dict()		
			list_system_VMsJSON = self.my_api.execute('listSystemVms')
			if('count' in list_system_VMsJSON.keys()):
				count = list_system_VMsJSON['count']
				if(count == 2):
					allRunning = True
					for systemVM in list_system_VMsJSON['systemvm']:
						allRunning = allRunning & (systemVM['state'] == 'Running')
					if(allRunning):
						keepRunning = False
			time.sleep(5)
		
		sec_grpJSON = self.my_api.execute('listSecurityGroups')
		sec_grp_id = sec_grpJSON['securitygroup'][0]['id']
		
		print "Changing Security Group Ingress Rules..."		
		params = dict()
		params['securitygroupid'] = sec_grp_id
		params['protocol'] = 'tcp'
		params['startport'] = '22'
		params['endport'] = '22'
		params['cidrlist'] = '0.0.0.0/0'
		self.my_api.execute('authorizeSecurityGroupIngress', params)
		
		params = dict()
		params['templatefilter'] = 'featured'
		list_templatesJSON = self.my_api.execute('listTemplates', params)	
		template_id = list_templatesJSON['template'][0]['id']
		
		service_offeringJSON = self.my_api.execute('listServiceOfferings')				
		for service_offering in service_offeringJSON['serviceoffering']:
			if(service_offering['name'] == 'Small Instance'):
				service_offering_id = service_offering['id']
		
		print "Deploying VM..."				
		params = dict()
		params['zoneid'] = zone_id
		params['templateid'] = template_id
		params['serviceofferingid'] = service_offering_id
		deploy_VMJSON = self.my_api.execute('deployVirtualMachine', params)
		
		#VM_id = deploy_VMJSON['id']
		
		print 'Dhatad tatad'

if __name__ == '__main__':
	cloud = CloudCreator()
