import os, json

def log(message):
  print(message)

class WikiPagesManager(object):
  '''
    Return a dict of key-values (page, children)
    where children are all the pages that point to page

    The datafile argument should be the output of the Hadoop job
  '''
  @staticmethod
  def get_nodes(datafile):
    with open(datafile) as thefile:
        lines = thefile.read().split('\n')
    nodes = {}

    for i in range(len(lines)):
        line = lines[i]
        split = line.split('\t')
        if len(split) < 2:
          continue
        name = split[1]
        if len(name):
          name = name[0].upper() + name[1:]
        values = [split[0]]
        if name in nodes:
          nodes[name].extend(values)
        else: nodes[name] = values

    return nodes

  def __init__(self, datafile):
    self.nodes = WikiPagesManager.get_nodes(datafile)
  
  '''
    Find next page for a certain wikipedia page
    by finding the page parent in the tree
  '''
  def get_next_page_for(self,page):
    for node,children in self.nodes.items():
      if page in children:
        return node

  '''
    Find series of next pages that you'd get by doing the 'getting to Philosophy'
    starting from a certain page.
    Stop when a loop is found or when we arrive at the wanted page
  '''
  def get_next_page_series(self, page, series = [], stopAt = 'Philosophy'):
    if(page in series):
      series.append(page)
      return series
    series.append(page)
    if(page == stopAt):
      return series

    page = self.get_next_page_for(page)

    return self.get_next_page_series(page, series, stopAt)

  '''
    Get a dict containing all the pages and their respective
    distance to a certain page
  '''
  def get_distances_to_page(self,page='Philosophy'):

    distances = {}

    stack = [{page: 0}]

    def process_node(name, distance):
      distances[name] = distance
      if name in self.nodes:
        for child in self.nodes[name]:
          stack.append({child: distance + 1})

    while len(stack):
      node, distance = stack.pop().items()[0]
      process_node(node, distance)

    return distances

def dumpDistancesToCertainPage(input, page='Philosophy'):
  output = input + '.json'

  log('Getting nodes from ' + input)
  wpm = WikiPagesManager(input)
  log('Getting nodes - Done')

  log('Getting all distances to ' + page)
  distances = wpm.get_distances_to_page(page)
  log('Getting distances - Done')

  log('Writing json output in ' + output)
  with open(output, 'w') as outfile:
    json.dump(distances, outfile)

  log('Writing output - Done')

if __name__ == '__main__':
  from sys import argv

  if len(argv) < 2:
    print('Please specify at least the Hadoop job output file')
    exit()

  input = argv[1]
  if len(argv) == 2:
    dumpDistancesToCertainPage(input)
  elif len(argv) == 3:
    dumpDistancesToCertainPage(input, argv[2])
