import os

def get_nodes():
	with open("result") as thefile:
	    lines = thefile.read().splitlines()

	nodes = {}

	for line in lines:
	    split = line.split('\t')
	    name = split[0]
	    if len(name):
	    	name = name[0].upper() + name[1:]
	    values = split[1:]
	    if name in nodes:
	    	nodes[name].extend(values)
	    else: nodes[name] = values

	return nodes

NODES = get_nodes()

def get_next_page_for(page):
	nc = 0
	result = None
	for node,children in NODES.items():
		if page in children:
			result = node
			nc += 1
	print nc
	return result

def get_next_page_series(page, series = [], stopAt = 'Philosophy'):
	if(page in series):
		series.append(page)
		return series
	series.append(page)
	if(page == stopAt):
		return series

	page = get_next_page_for(page)
	return get_next_page_series(page, series, stopAt)


def get_distance_to_page(page='Philosophy'):
	philo_distance = {}
	def process_node(name, distance):
		philo_distance[name] = distance
		if name in NODES:
			for child in NODES[name]:
				process_node(child, distance + 1)

	process_node(page, 0)

	return philo_distance