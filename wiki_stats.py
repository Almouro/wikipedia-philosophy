import os, json

def log(message):
  print(message)

class WikiPagesManager(object):
  '''
    Return a dict of key-values (page, children)
    where children are all the pages that point to page

    The datafile argument should be the output of the Hadoop job
  '''
  def get_nodes(self, datafile):
    with open(datafile) as thefile:
        lines = thefile.read().split('\n')

    self.nodes = {}
    self.pages = {}
    self.nb_pages = 0
    
    print str(len(lines)) + " pages found"
    for i in range(len(lines)):
        line = lines[i]
        split = line.split('\t')

        if len(split) < 2:
            continue 

        name = split[0]
        if len(name):
          name = name[0].upper() + name[1:] 
        
        values = [split[1]]
        is_redirect = len(split) > 2

        if not is_redirect:
          self.nb_pages += 1

        self.pages[split[1]] = {
          "is_redirect": is_redirect,
          "next_page": name
        }
        
        if name in self.nodes:
          self.nodes[name].extend(values)
        else: self.nodes[name] = values

    print str(self.nb_pages) + " (not redirected) pages found"

  def __init__(self, datafile):
    self.get_nodes(datafile)
  
  '''
    Find next page for a certain wikipedia page
  '''
  def get_next_page_for(self,page):
    return self.pages[page]["next_page"]

  '''
    Find series of next pages that you'd get by doing the 'getting to Philosophy'
    starting from a certain page.
    Stop when a loop is found or when we arrive at the wanted page
  '''
  def get_next_page_series(self, page, series = [], stopAt = 'Philosophy'):
    print page
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

#     stack = [{page: 0}]

    def process_node(name, distance):
      if name in distances:
          return

      if not self.pages[name]["is_redirect"]:
        distances[name] = distance

      if name in self.nodes:
        for child in self.nodes[name]:
            process_node(child, distance + 1)
#           stack.append({child: distance + 1})

#     while len(stack):
#       node, distance = stack.pop().items()[0]
#       process_node(node, distance)
    
    process_node(page, 0)
    #print str(len(distances.keys()) * 100 / self.nb_pages) + '% success'
    return distances

  def get_getting_to_page_ratio(self):
    i = 0
    ratios = {}

    for page in wpm.pages:
      i += 1
      if i % 100 == 0:
        print i
      if self.pages[page]["is_redirect"]:
        continue

      n = len(self.get_distances_to_page(page).keys())
      ratios[page] = n

    return ratios

  def dump_pages_ratios(self, output=None):
    if not output:
      output = "ratios.json"

    log('Getting all ratios')
    ratios = wpm.get_getting_to_page_ratio()
    log('Getting ratios - Done')

    ratios = {
      "pages": self.nb_pages,
      "ratios": ratios
    }

    log('Writing json output in ' + output)
    with open(output, 'w') as outfile:
      json.dump(ratios, outfile)

    log('Writing output - Done')

  def dump_distances_to_page(self, page='Philosophy', output=None):
    if not output:
      output = page + "_distances.json"

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
    print('Please specify the Hadoop job output file')

  else:
    input = argv[1]
    log('Getting nodes from ' + input)
    wpm = WikiPagesManager(input)
    log('Getting nodes - Done')

