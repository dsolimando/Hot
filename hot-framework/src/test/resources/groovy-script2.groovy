class Personne {
	def nom
	def prenom
	def adresse
	def age
}

def personnes = []
for (n=0;n<i;++n) {
	def p = new Personne()
	p.nom = "nom${n}"
	p.prenom = "prenom${n}"
	p.adresse = "adresse${n}"
	p.age = n%30
	personnes << p
}
//Thread.sleep (30)
return personnes.size()