import java

class Personne:
	nom = ""
	prenom = ""
	adresse = ""
	age = ""

personnes = []
for n in range (1,i+1):
	p = Personne()
	p.nom = "nom"+str(n)
	p.prenom = "prenom"+str(n)
	p.adresse = "adresse"+str(n)
	p.age = n%30
	personnes.append (p)

result = len(personnes)
#java.lang.Thread.sleep(30)
#personnes = None
