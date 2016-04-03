import sys

def call(a,b):
	a=int(a)
	b=int(b)
	a=a+1
	b=b+1
	return a,b

def call(a,b):
	a=int(a)
	b=int(b)
	c=float(a)/b
	a=a+1
	b=b+1
	return [a,b,c]

def main():
	try:
		arglist = call(sys.argv[1],sys.argv[2])
		print arglist
	except Exception, e:
		print e
	else:
		print 'k else'
	finally:
		print 'k finally'

if __name__ == '__main__':
	main()