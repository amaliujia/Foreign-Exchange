import sys,os
import csv
import math

# define the node
class Node:
	y = 0
	n = 0
	nodes = []
	leftyes = 0
	rightyes = 0
	rightno = 0
	leftno = 0
	attr_left = None
	attr_right = None
	attr = None
	left_label = -1
	right_label = -1

def readCVS(filename):
	reader = csv.reader(file(filename, 'rb'))
	return reader

# transfer row data to instances
def createInstancs(reader):
	instances = []
	label = []
	attr = []
	attr_value = {} 
	first = True;
	for line in reader:
		if not first :
			instances.append(line)
			if line[len(line) - 1] not in label:
				label.append(line[len(line) - 1])
			for i in range(0, len(line) - 1):
				if not attr_value.get(attr[i]):
					attr_value[attr[i]] = []
					attr_value[attr[i]].append(line[i])
				else:
					if line[i] not in attr_value[attr[i]]:
						attr_value[attr[i]].append(line[i])	
		else:
			attr = line
			first = False
	return instances, label, attr, attr_value;

# compute the cross entropy.
def entropy(yes, no):
		total = yes + no
		conyes = yes * 1.0 / total
		conno = no * 1.0 / total
		x = 0
		y = 0
		if(conyes == 0 and conno == 0):
			return 0
		if(conyes == 0):
			return - conno * math.log(conno, 2)
		if(conno == 0):
			return -1 * conyes * math.log(conyes, 2)
		return -1 * conyes * math.log(conyes, 2) - conno * math.log(conno, 2)

def mentroy(instances, labels, attr, attr_values, index = None, attr_name = None, exclude_attr = None):
	yes = labels[0] 
	no = labels[1]
	y = 0
	n = 0
	if index == None :
		for i in instances:
			if i[len(i) - 1] == yes:
				y += 1
			elif i[len(i) - 1] == no:
				n += 1
        return entropy(y, n), y, n 

def mentroy_t(instances, labels, attr, attr_values, index = None, attr_name = None, exclude_attr = None):
	value = attr_name
	yes = labels[0] 
	no = labels[1]
	y = 0
	n = 0
	#for value in attr_values[attr_name]:
	for instance in instances:
		if instance[index] == value:
			if instance[len(instance) - 1] == yes:
				y += 1
			elif instance[len(instance) - 1] == no:
				n += 1
	return entropy(y, n), y, n

def mentroy_h(instances, labels, attr, attr_values, index, index2, attr_name, hostAttr):
	value = attr_name
	yes = labels[0] 
	no = labels[1]
	y = 0
	n = 0
	for instance in instances:
		#print index, index2, instance[index], instances[index2] 
		if instance[index] == value and instance[index2] == hostAttr:
			#print instance
			if instance[len(instance) - 1] == yes:
				y += 1
			elif instance[len(instance) - 1] == no:
				n += 1
	#print y, n
	return entropy(y, n), y, n

# the function that is used to split node
def subNode(instances, labels, attr, attr_values, hostAttr, hostAttrValue, rootleft, rootright):
	node = Node()
	maxAttr = None
	maxValue = -1.0
	total = rootleft + rootright
	en = entropy(rootleft, rootright)
	for i in range(0, len(attr) - 1):
		a = attr[i]
		if a == hostAttr:
			continue
		#print hostAttr, hostAttrValue
		temp1, z, x = mentroy_h(instances, labels, attr, attr_values, attr.index(a), attr.index(hostAttr), attr_values[a][0], hostAttrValue)
		temp2, c, d = mentroy_h(instances, labels, attr, attr_values, attr.index(a), attr.index(hostAttr), attr_values[a][1], hostAttrValue)
		gain = (en - (z + x + 0.0) / total * 1.0 * temp1 - (c + d + 0.0) / total * 1.0 * temp2)
		#print gain, a, z, x, c, d
		if gain > 0.1 and gain > maxValue:
			maxValue = gain
			maxAttr = a		
	if maxValue == -1.0:
		return None
	temp1, z, x = mentroy_h(instances, labels, attr, attr_values, attr.index(maxAttr), attr.index(hostAttr), attr_values[maxAttr][0], hostAttrValue)
	temp2, c, d = mentroy_h(instances, labels, attr, attr_values, attr.index(maxAttr), attr.index(hostAttr), attr_values[maxAttr][1], hostAttrValue)
	#print gain, maxAttr, z, x, c, d
	node.leftyes = z
	node.leftno = x
	node.rightyes = c
	node.rightno = d
	node.attr_left = attr_values[maxAttr][0]
	node.attr_right = attr_values[maxAttr][1]
	node.attr = maxAttr
	node.left_label = labels[0] if node.leftyes > node.leftno else labels[1]
	node.right_label = labels[0] if node.rightyes > node.rightno else labels[1]
	return node

# build the binary decision tree based on row data.
def buildTree(instances, labels, attr, attr_values, exclude_attr = None):
	root = Node()
	maxAttr = None
	maxValue = -1.0
	entropy, root.y, root.n = mentroy(instances, labels, attr, attr_values)
	total = root.y + root.n 
	for i in range(0, len(attr) - 1):
		a = attr[i]
		#print attr.index(a), attr_values[a][1]
		if (exclude_attr == None) or (exclude_attr != None and a not in exclude_attr):
			temp1, z, x = mentroy_t(instances, labels, attr, attr_values, attr.index(a), attr_values[a][0])
			temp2, c, d = mentroy_t(instances, labels, attr, attr_values, attr.index(a), attr_values[a][1])
			gain = (entropy - (z + x + 0.0) / total * 1.0 * temp1 - (c + d + 0.0) / total * 1.0 * temp2)
			#print gain
			if gain > 0.1 and gain > maxValue:
				maxValue = gain
				maxAttr = a
	if maxValue == -1.0:
		return root
	temp1, z, x = mentroy_t(instances, labels, attr, attr_values, attr.index(maxAttr), attr_values[maxAttr][0])
	temp2, c, d = mentroy_t(instances, labels, attr, attr_values, attr.index(maxAttr), attr_values[maxAttr][1])
	root.leftyes = z
	root.leftno = x
	root.rightyes = c
	root.rightno = d
	root.attr_left = attr_values[maxAttr][0]
	root.attr_right = attr_values[maxAttr][1]
	root.attr = maxAttr
	leftleaf = subNode(instances, labels, attr, attr_values, root.attr, root.attr_left, root.leftyes, root.leftno)
	rightleaf = subNode(instances, labels, attr, attr_values, root.attr, root.attr_right, root.rightyes, root.rightno)
	if leftleaf == None:
		root.left_label = labels[0] if root.leftyes > root.leftno else labels[1]
		root.nodes.append(None)
	else:	
		root.nodes.append(leftleaf)
	if rightleaf == None:
		root.right_label = labels[0] if root.rightyes > root.rightno else labels[1]
		root.nodes.append(None)
	else:
		root.nodes.append(rightleaf)
	return root

def printTree(root):
	sys.stdout.write("["+str(root.y)+"+/" + str(root.n) + "-]\n")
	sys.stdout.write(root.attr + " = " + root.attr_left + ": ["+str(root.leftyes)+"+/" + str(root.leftno) + "-]\n")
	if(root.nodes[0] != None):
		sys.stdout.write("| " +root.nodes[0].attr + " = " + root.nodes[0].attr_left + ": ["+str(root.nodes[0].leftyes)+"+/" + str(root.nodes[0].leftno) + "-]\n")
		sys.stdout.write("| " +root.nodes[0].attr + " = " + root.nodes[0].attr_right + ": ["+str(root.nodes[0].rightyes)+"+/" + str(root.nodes[0].rightno) + "-]\n")
	sys.stdout.write(root.attr + " = " + root.attr_right + ": ["+str(root.rightyes) + "+/" + str(root.rightno) + "-]\n")
	if(root.nodes[1] != None):
		sys.stdout.write("| " +root.nodes[1].attr + " = " + root.nodes[1].attr_left + ": ["+str(root.nodes[1].leftyes)+"+/" + str(root.nodes[1].leftno) + "-]\n")
		sys.stdout.write("| " +root.nodes[1].attr + " = " + root.nodes[1].attr_right + ": ["+str(root.nodes[1].rightyes)+"+/" + str(root.nodes[1].rightno) + "-]\n")


# vote function, used to give the label for prediction.
def vote(reader, root):
	total = 0
	error = 0
	isFirst = True
	labels = []
	for line in reader:
		if isFirst:
			isFirst = False
			labels = line
		else:
			attr = root.attr
			index = labels.index(attr)
			value = line[index]
			if value == root.attr_left:
				if root.nodes[0] != None:
					cattr = root.nodes[0].attr
					index2 = labels.index(cattr)
					cvalue = line[index2]
					if cvalue == root.nodes[0].attr_left:
						if root.nodes[0].left_label != line[len(line) - 1]:
							error += 1
						total += 1
					else:
						if root.nodes[0].right_label != line[len(line) - 1]:
							error += 1
						total += 1
				else:
					if root.left_label != line[len(line) - 1]:
						error += 1
					total += 1
			else:
				if root.nodes[1] != None and root.nodes[1].attr != None:
					cattr = root.nodes[1].attr
					index2 = labels.index(cattr)
					cvalue = line[index2]
					if cvalue == root.nodes[1].attr_left:
						if root.nodes[1].left_label != line[len(line) - 1]:
							error += 1
						total += 1
					else:
						if root.nodes[1].right_label != line[len(line) - 1]:
							#print root.nodes[1].right_label, line
							error += 1
						total += 1
				else:
					if root.right_label != line[len(line) - 1]:
						error += 1;
					total += 1
	return error * 1.0  / (total * 1.0)				


def main():
	reader = readCVS(sys.argv[1])
	instances, labels, attr, attr_value = createInstancs(reader)
	root = buildTree(instances, labels, attr, attr_value)
	printTree(root)
	print "error(train): " + str(vote(readCVS(sys.argv[1]), root))
	print "error(test): " + str(vote(readCVS(sys.argv[2]), root))


main()
