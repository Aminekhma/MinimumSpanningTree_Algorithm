package algorithms;

import java.awt.Point;
import java.util.ArrayList;


public class DefaultTeam {

	// calculer steiner sans restriction
	public Tree2D calculSteiner(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints) {


		System.out.println("edgeThreshold : "+ edgeThreshold);
		System.out.println("hitPoints : "+ hitPoints.size());
		System.out.println("points : "+ points.size());

		
		// trouver les plus courts chemins du graphe 
		int[][] liste_chemins = calculPlusCourtChemins(points, edgeThreshold);

		// appliquer l'algorithme steiner et récupérer les arretes de l'arbre resultat
		ArrayList<Edge> liste_arretes_finale = recupererArretesSteinerSansBudget(points, liste_chemins, hitPoints);

		// générer l'arbre steiner à partir des arretes déjà créées
		return genererTree2D(liste_arretes_finale, liste_arretes_finale.get(0).p);
	}


	public Tree2D calculSteinerBudget(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints) {
		
		// trouver les plus courts chemins du graphe
		int[][] liste_chemins = calculPlusCourtChemins(points, edgeThreshold);
		
		// budget à respecter 
		double budget = 3000;
		
		// appliquer l'algorithme steiner avec resriction  et récupérer les arretes de l'arbre resultat
		ArrayList<Edge> liste_arretes_finale = recupererArretesSteinerAvecBudget(points, liste_chemins, hitPoints, budget);
		
		
		

		// générer l'arbre steiner avec restrictions à partir des arretes déjà créées
		return genererTree2D(liste_arretes_finale, liste_arretes_finale.get(0).p);  
	}


	/**
	 * Retrouver les plus courts chemin du graphe en applicant l'algorithme de Floyd-Warshall
	 * @param points : les points du graphe
	 * @param edgeThreshold : seuil de distance max
	 * @return
	 */
	public int[][] calculPlusCourtChemins(ArrayList<Point> points, int edgeThreshold) {
		int[][] liste_chemins = new int[points.size()][points.size()];
		for (int i = 0; i < liste_chemins.length; i++)
			for (int j = 0; j < liste_chemins.length; j++)
				liste_chemins[i][j] = i;

		//  la liste des distances entre les points du graphe
		double[][] distances = new double[points.size()][points.size()];

		// remplir les distances entre chaque 2 points du graphe
		for (int i = 0; i < liste_chemins.length; i++) {
			for (int j = 0; j < liste_chemins.length; j++) {
				// un point a 0 comme distance avec lui même
				if (i == j) {
					distances[i][i] = 0;
				}else {
					// si on a pas atteint le seiul , on calcul la listance entre 2 points avec la méthode distance 
					if (points.get(i).distance(points.get(j)) <= edgeThreshold)
						distances[i][j] = points.get(i).distance(points.get(j));
					else {
						// sinon, on met que la distance est enormément grande [infinie]
						distances[i][j] = Double.POSITIVE_INFINITY;
					}
					// on relie un chemin entre i et j
					liste_chemins[i][j] = j;
				}
			}
		}
		// filtrer les chemins selon la distance entre chaque 2 points, que les plus petite  distances restent 
		for (int k = 0; k < liste_chemins.length; k++) {
			for (int i = 0; i < liste_chemins.length; i++) {
				for (int j = 0; j < liste_chemins.length; j++) {
					if (distances[i][j] > distances[i][k] + distances[k][j]) {
						distances[i][j] = distances[i][k] + distances[k][j];
						liste_chemins[i][j] = liste_chemins[i][k];
					}
				}
			}
		}
		return liste_chemins;
	}


	// appliquer l'algorithme du kruskal pour obtenir la liste des arretes representant le minimum spanning Tree du graphe
	public static ArrayList<Edge> appliquerKruskal(ArrayList<Point> points, ArrayList<Point> hitPoints, int[][] liste_chemins) {

		ArrayList<Edge> liste_arretesMST = new ArrayList<Edge>();

		System.out.print("kruskalHitPointEdges  : ");
		for (Point p : hitPoints) {
			for (Point q : hitPoints) {
				// pour chaque 2 points distincts, verieifer si on a traité l'arrete associé
				if (p.equals(q) || edgeAppartient(liste_arretesMST, p, q)) {
					//System.out.print("Edge non ajouté : "+(new Edge(p,q)));
				}else {
					// si non, on ajoute l'arrete
					Edge arrete = new Edge(p, q);
					arrete.setDistance(distanceCheminPlie(p, q, liste_chemins, points));
					liste_arretesMST.add(arrete);
					//System.out.print("Edge ajouté : "+edge);
				}
			}
		}
		// trier la liste
		liste_arretesMST = trierListeArretes(liste_arretesMST);
		
		// retourner l'arbre resultat
		return liste_arretesMST;
	}


	// lancer le process pour tager les points et les arretes 
	public static ArrayList<Edge> taggingEdges(ArrayList<Point> points, ArrayList<Edge> liste_arretes) {
		ArrayList<Edge> liste_arretes_finale = new ArrayList<Edge>();
		NameTag tag_points = new NameTag(points);
		// pour chaque arrete, tagger les points associés
		while (liste_arretes.size() != 0) {
			Edge arrete = liste_arretes.remove(0);
			if (tag_points.tag(arrete.p) != tag_points.tag(arrete.q)) {
				liste_arretes_finale.add(arrete);
				tag_points.reTag(tag_points.tag(arrete.p), tag_points.tag(arrete.q));
			}
		}
		
		// retourner de nouveau la liste des arretes après avoir été taggé 
		return liste_arretes_finale;

	}


	
	// calculer la distance d'unchemin plié passant par plusieur points
	public static double distanceCheminPlie(Point p, Point q, int[][] liste_chemins, ArrayList<Point> points) {
		// recuperer les indice des points dans la liste
		int i = points.indexOf(p);
		int j = points.indexOf(q);
		if (i == j)
			// on ne fait rien si p et le même point que q
			return 0;
		
		// sinon, on calcule la listance en avançant d'une manière recursive
		int i_suivant = liste_chemins[i][j];
		return points.get(i).distance(points.get(i_suivant)) + distanceCheminPlie(points.get(i_suivant), q, liste_chemins, points);
	}



	// verifier si 2 point construit un edge appartenant à la liste 
	public static boolean edgeAppartient(ArrayList<Edge> liste_arretes, Point p, Point q) {
		for (Edge e : liste_arretes) {
			if (e.p.equals(p) && e.q.equals(q) || e.p.equals(q) && e.q.equals(p))
				return true;
		}
		return false;
	}


	
	// générer un arbre de la classe Tree2D à partir de la liste des arretes passés en paramètre
	public static Tree2D genererTree2D(ArrayList<Edge> liste_arretes, Point point_racine) {
		ArrayList<Edge> liste_arretes_restantes = new ArrayList<Edge>();
		ArrayList<Point> liste_points_racines = new ArrayList<Point>();

		// jusqu'à vider la liste des arrete, decider ou mettre chaque arrete, (soit racine ou autre)
		while (liste_arretes.size() != 0) {
			Edge arrete = liste_arretes.remove(0);
			// System.out.print("genererTree2D  : arrete :  ");
			if (arrete.p.equals(point_racine)) {
				liste_points_racines.add(arrete.q);
			} else {
				if (arrete.q.equals(point_racine)) {
					liste_points_racines.add(arrete.p);
				} else {
					liste_arretes_restantes.add(arrete);
				}
			}
		}

		ArrayList<Tree2D> liste_arbres = new ArrayList<Tree2D>();
		// créer les sous arbres par rapport aux points racines 
		for (Point p : liste_points_racines) {
			ArrayList<Edge> liste_arretes_restantes_copie = new ArrayList<Edge>();
			for (Edge e: liste_arretes_restantes) {
				liste_arretes_restantes_copie.add(e.clone());
				System.out.print("> final edge list  :  "+ e);
			}
			liste_arbres.add(genererTree2D(liste_arretes_restantes_copie, p));
		}

		return new Tree2D(point_racine, liste_arbres);
	}



	// trier la liste des arretes selon la distance de chacune
	public static ArrayList<Edge> trierListeArretes(ArrayList<Edge> liste_arretes) {
		
		// si la liste est vide ou contient une arrete, on la retourne telle qu'elle est
		if (liste_arretes.size() <= 1)
			return liste_arretes;
		// sinon, on divise l'ensemble sur 2 sous ensembles
		ArrayList<Edge> liste_arrete_gauche = new ArrayList<Edge>();
		ArrayList<Edge> liste_arretes_droite = new ArrayList<Edge>();
		int n = liste_arretes.size();
		
		// remplir la liste des arrete à gauche 
		for (int i = 0; i < n / 2; i++) {
			liste_arrete_gauche.add(liste_arretes.remove(0));
		}
		// remplir la liste des arrete à droite avec le reste
		while (liste_arretes.size() != 0) {
			liste_arretes_droite.add(liste_arretes.remove(0));
		}
		
		// ordonner les deux liste des arretes
		liste_arrete_gauche = trierListeArretes(liste_arrete_gauche);
		liste_arretes_droite = trierListeArretes(liste_arretes_droite);

		// selon la distance commencer à prendre les arrete avec la distance minimale
		ArrayList<Edge> liste_arrete_resultat = new ArrayList<Edge>();
		while (liste_arrete_gauche.size() != 0 || liste_arretes_droite.size() != 0) {
			if (liste_arrete_gauche.size() == 0) {
				liste_arrete_resultat.add(liste_arretes_droite.remove(0));
				continue;
			}
			if (liste_arretes_droite.size() == 0) {
				liste_arrete_resultat.add(liste_arrete_gauche.remove(0));
				continue;
			}
			if (liste_arrete_gauche.get(0).getDistance() < liste_arretes_droite.get(0).getDistance())
				liste_arrete_resultat.add(liste_arrete_gauche.remove(0));
			else
				liste_arrete_resultat.add(liste_arretes_droite.remove(0));
		}
		return liste_arrete_resultat;
	}




	// récupérer l'arbre steiner sans restriction
	public static ArrayList<Edge> recupererArretesSteinerSansBudget(ArrayList<Point> points, int[][] liste_chemins, ArrayList<Point> hitPoints) {

		ArrayList<Edge> resultat = new ArrayList<>();
		
		// recupérer la liste des arretes généré par l'algorithle de  spanning tree de kruskal
		ArrayList<Edge> arretes_kruskal = appliquerKruskal(points, hitPoints, liste_chemins);

		// tagger l'ensemble des arretes restantes
		arretes_kruskal = taggingEdges(points, arretes_kruskal);
		
		// pour chaque arrete verifier la liste des chemins de ses points 
		for (Edge arrete : arretes_kruskal) {
			
			// récupérer les indices des point p et q de la liste
			int p = points.indexOf(arrete.p);
			int q = points.indexOf(arrete.q);
			
			// créer la liste des arretes relié aux points de l'arrete actuelle
			ArrayList<Edge> arretes_transposees = new ArrayList<>();
			
			// garder les position actuelle et prochaine de p
			int p_actuelle = p;
			int p_suivante= liste_chemins[p_actuelle][q];
			do {
				Edge arrete2 = new Edge(points.get(p_actuelle), points.get(p_suivante));
				arretes_transposees.add(arrete2);
				p_actuelle = p_suivante;
				p_suivante = liste_chemins[p_suivante][q];
			} while (p_actuelle != q);

			resultat.addAll(arretes_transposees);

		}

		return resultat;
	}
	
	// recupérer l'arbre steiner avec restriction sur le budget introduit en parametre
	public static ArrayList<Edge> recupererArretesSteinerAvecBudget(ArrayList<Point> points, int[][] liste_chemins, ArrayList<Point> hitPoints, double budget) {

		// créer l'arbre steiner sans restrictions
		ArrayList<Edge> resultat = recupererArretesSteinerSansBudget(points, liste_chemins, hitPoints);
		
		// parcourir la liste tant qu'on est au dessus du budget
		double somme_distances = 0;
		do {
			somme_distances = budgetTotal(resultat);
			System.out.println("budgetTotal : "+somme_distances);
			// récupérer l'arrete avec la distance max 
			Edge max_e = maxEdge(resultat);
			System.out.println("max_e : "+max_e + " - " + max_e.getDistance());
			// enlever cette arrete
			resultat.remove(max_e);
			
		}while(somme_distances > budget);
		
		
		return resultat;
	}
		
	// calculer budget des aretes d'un graphe
	private static double budgetTotal(ArrayList<Edge> arretes) {
		double total = 0;
		for(Edge e: arretes) {
			total += e.getDistance();
		}
		return total;
	}	
	
	// retourner l'arrete avec plus de distance
	private static Edge maxEdge(ArrayList<Edge> arretes) {
		// si liste vide, retourner null
		if (arretes.size() == 0) {
			return null;
		}
		// parcourir et chercher l'arrete avec max distance
		Edge max = arretes.get(0);
		for(Edge e: arretes) {
			if( e.getDistance() > max.getDistance()) {
				max = e;
			}
		}
		return max;
	}

}

	


// ------------------> Classe Edge   <-------------------- //
class Edge {
	// attribut d'une arrete : p, q et la distance entre les deux
	public Point p, q;
	public double dist;

	// constructeur d'une arrete a deux point p et q initiaux
	public Edge(Point p, Point q) {
		this.p = p;
		this.q = q;
		this.dist = p.distance(q);
	}
	
	// recupérer la distance entre p et q
	public double getDistance() {
		return this.dist;
	}	
	// modifier la distance entre p et q
	public void setDistance(double dist) {
		this.dist = dist;
	}

	// redifinir comment faire une copie d'une arrete
	@Override
	protected Edge clone() {
		Edge e2 = new Edge(new Point(p.x, p.y), new Point(q.x, q.y));
		e2.dist = dist;
		return e2;
	}

	// redifinir comment s'affiche une arrete
	@Override
	public String toString() {
		// 
		return "("+p.x+","+p.y +") --("+dist+")--> " + "("+q.x+","+q.y +")";
	}

}



//------------------> Classe NameTag   <-------------------- //
// sert à tagger et identifier chacun des points 
class NameTag {
	private ArrayList<Point> points;
	private int[] tag;

	protected NameTag(ArrayList<Point> points) {
		ArrayList<Point> clone = new ArrayList<Point>();
		for (Point e: points) {
			clone.add(new Point(e.x, e.y));
		}
		this.points = clone;
		tag = new int[points.size()];
		for (int i = 0; i < points.size(); i++)
			tag[i] = i;
	}

	protected void reTag(int j, int k) {
		for (int i = 0; i < tag.length; i++)
			if (tag[i] == j)
				tag[i] = k;
	}

	protected int tag(Point p) {
		for (int i = 0; i < points.size(); i++)
			if (p.equals(points.get(i)))
				return tag[i];
		return 0xBADC0DE;
	}

}