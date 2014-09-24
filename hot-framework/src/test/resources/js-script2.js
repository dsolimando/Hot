function Personne () {
	this.nom = "";
	this.prenom = "";
	this.adresse = "";
	this.age = 0;
}

var personnes = []
for (n=0;n<i;++n) {
	var p = new Personne ();
	p.nom = "nom"+n;
	p.prenom = "prenom"+n;
	p.adresse = "adresse"+n;
	p.age = n;
	personnes.push(p);
}
personnes.length;