import os

class WikiStats(object):
	@staticmethod
	def get_nodes(datafile):
		with open(datafile) as thefile:
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

	def __init__(self, datafile):
		self.nodes = WikiStats.get_nodes(datafile)

	def get_next_page_for(self,page):
		for node,children in self.nodes.items():
			if page in children:
				return node

	def get_next_page_series(self, page, series = [], stopAt = 'Philosophy'):
		if(page in series):
			series.append(page)
			return series
		series.append(page)
		if(page == stopAt):
			return series

		page = self.get_next_page_for(page)

		return self.get_next_page_series(page, series, stopAt)


	def get_distances_to_page(self,page='Philosophy'):
		distances = {}

		def process_node(name, distance):
			distances[name] = distance
			if name in self.nodes:
				for child in self.nodes[name]:
					process_node(child, distance + 1)

		process_node(page, 0)

		return distances
