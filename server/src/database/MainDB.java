package database;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * cette classe a juste pour objectif de verifier les noms des methodes
 */

class MainDB {
    public static void main(String[] args) throws SQLException, ServeurIncorrectException, ServeurActionNonPermiseException, ServeurNonIdentifieException, InterruptedException {

        // on créer un serveur agrédité
        Serveur s = null;
        boolean isConnecte = false;
        try {
            s = new Serveur("user1@mail.com", "$fg#;mdp$$$$$0");
            isConnecte = true;
        } catch (ServeurIncorrectException e) {
            System.out.println("Connexion échoué : mot de passe incorrect");
        }

        if (isConnecte) {
            // on lance notre service dans notre annuaire
            s.lancerService();

            // on inscrit notre service au service central
            s.inscrireService("127.0.0.1", 1235);
        }
/*        Scanner scanner = new Scanner(System.in);
        Serveur s = null;
        String mail;
        String mdp;

        int choix;
        boolean bonChoix;
        boolean connecte = false;*/

        /*
        // test mauvaise connexion
        try {
            s = new Serveur("fake", "fake");
        } catch (ServeurIncorrectException e) {
            System.out.println("Connexion incorrecte : normal");
        }

        // test bonne connexion
        try {
            s = new Serveur("user1@mail.com", "$fg#;mdp$$$$$0");
        } catch (ServeurIncorrectException e) {
            System.out.println("Connexion incorrecte : PAS normal");
        }

        s = new Serveur("user1@mail.com", "$fg#;mdp$$$$$0");
        System.out.println(s.consulterTable("2019-12-12", "12"));

        System.out.println(s.consulterPlatsDispo());

        System.out.println(s.consulterAffectation());

         */

        /*System.out.println(" --- Bienvenue sur le menu de Miaam --- ");
        System.out.println("Veuillez d'abord vous connecter : ");


        while (!connecte) {
            System.out.print("\nEntrez votre adresse mail : ");
            mail = scanner.nextLine();
            System.out.print("Entrez votre mot de passe : ");
            mdp = scanner.nextLine();
            try {
                s = new Serveur(mail, mdp);
                connecte = true;
            } catch (ServeurIncorrectException e) {
                System.out.println("Adresse ou mot de passe incorrect");
            }
        }

        boolean continuer = true;
        while (continuer) {
            choix = 99;
            bonChoix = false;
            afficherMenu();

            while (!bonChoix) {
                try {
                    System.out.print("\n Votre choix : ");
                    choix = scanner.nextInt();
                    scanner.nextLine(); // permet de vider le buffer
                    if (1 <= choix && choix <= 7) {
                        bonChoix = true;
                    } else {
                        System.out.println("Veuillez entrer un choix valide entre 1 et 7");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Veuillez entrer un choix valide entre 1 et 7");
                    scanner.nextLine();
                } catch (Exception e) {
                    System.out.println("Erreur inconnue dans le choix");
                    scanner.nextLine();
                }
            }

            switch (choix) {
                case 1:  // Consulter les table disponibles
                    String date = null, heure = null;
                    System.out.println(" -- Consulter les table disponibles -- ");

                    date = demanderDate(scanner);
                    heure = demanderHeure(scanner);

                    System.out.println(s.consulterTable(date, heure));
                    break;

                case 2:  // Réserver une table
                    int numtab = -1;
                    date = null;
                    heure = null;

                    System.out.println(" -- Réserver une table -- ");

                    date = demanderDate(scanner);
                    heure = demanderHeure(scanner);

                    boolean verifTable = false;
                    while (!verifTable) {
                        numtab = demanderTable(scanner);  // demande ET verif si table existe dans la BDD
                        if (!Table.isDispoByDate(date, heure, numtab)) {  // verif si table dispo à cette date et heure
                            System.out.println("Table non disponible à cette date et heure, veuillez en choisir une autre");
                        } else {
                            verifTable = true;
                        }
                    }
                    if (s.reserverTable(numtab, date, heure)) {
                        System.out.printf("Table %d réservée avec succès le %s à %sH\n", numtab, date, heure);
                    } else {
                        System.out.println("Erreur lors de la réservation");
                    }

                    break;

                case 3:  // Consulter les plats disponibles
                    System.out.println(" -- Consulter les plats disponibles -- ");
                    System.out.println(s.consulterPlatsDispo());
                    break;
                case 4:  // Commander des plats
                    *//* public boolean commanderPlats(int numres, int numplat, int qty) *//*
                    int numRes = demanderReservation(scanner);
                    int[] platAndQte = demanderPlat(scanner);
                    int numPlat = platAndQte[0];
                    int qte = platAndQte[1];

                    if (s.commanderPlats(numRes, numPlat, qte)) {
                        System.out.printf("Plat %d commandé en %dx avec succès pour la réservation %d\n", numPlat, qte, numRes);
                    } else {
                        System.err.println("Erreur lors de la commande");
                    }
                    break;
                case 5:
                    System.out.println(" -- Consulter les affectations des serveurs -- ");
                    System.out.println(s.consulterAffectation());
                    break;
                case 6:  // affecter des serveurs à des table
                    System.out.println(" -- Affecter des serveurs à des table -- ");

                    int numServ = demanderServeur(scanner);
                    int numTable = demanderTable(scanner);
                    date = demanderDate(scanner);

                    s.affecterServeur(numServ, numTable, date);
                    break;
                case 7:  // Calculer le montant total d’une réservation consommée (numéro de réservation) et mettre à jour la table RESERVATION pour l’encaissement.
                    System.out.println(" -- Calculer le montant total d’une réservation consommée -- ");

                    boolean finish = false;
                    while (!finish) {
                        numRes = demanderReservation(scanner);
                        finish = s.calculerMontantPourEncaissement(numRes);
                        if (!finish) {
                            System.err.println("Erreur lors de l'encaissement, contactez le support");
                        }
                    }
                    break;
            }

            scanner.nextLine();  // vider le buffer
            boolean stopRep = false;
            while (!stopRep) {
                System.out.print("Voulez-vous executer une autre requête ? (O/N) : ");
                String continuerRep = scanner.nextLine();
                if (continuerRep.equalsIgnoreCase("N")) {
                    stopRep = true;
                    continuer = false;
                    System.out.println("Au revoir !");
                } else {
                    if (continuerRep.equalsIgnoreCase("O")) {
                        stopRep = true;
                    } else {
                        System.out.println("Réponse invalide : Entrez O pour Oui ou N pour Non");
                    }
                }
            }
        }
        scanner.close();
    }

    public static void afficherMenu() {
        System.out.println("  --- Action Serveur --- ");
        System.out.println("1. Consulter les table disponibles");
        System.out.println("2. Réserver une table");
        System.out.println("3. Consulter les plats disponibles");
        System.out.println("4. Commander des plat");

        System.out.println("  ---  Action Gestionnaire --- ");
        System.out.println("5. Consulter les affectations des serveurs");
        System.out.println("6. Affecter des serveurs à des table");
        System.out.println("7. Calculer le montant total d’une réservation consommée (numéro de réservation) et mettre à jour la table RESERVATION pour l’encaissement.");
    }

    private static int demanderServeur(Scanner scanner) {
        int numServ_entree = -1;
        boolean numServValide = false;
        while (!numServValide) {
            System.out.print("Entrez le numéro du serveur : ");
            try {
                numServ_entree = scanner.nextInt();
                if (!Serveur.isExist(numServ_entree)) {
                    System.out.println("Serveur inexistant");
                } else {
                    numServValide = true;
                }
            } catch (Exception e) {
                System.out.println("Numéro de serveur invalide : Entrez un nombre entier.");
                scanner.nextLine();  // vider le buffer
            }
        }
        return numServ_entree;
    }

    public static String demanderDate(Scanner scanner) {
        String date_entree = null;
        boolean dateValide = false;
        while (!dateValide) {
            System.out.print("Entrez la date (AAAA-MM-JJ) : ");
            date_entree = scanner.nextLine();
            dateValide = verifDate(date_entree);
        }
        return date_entree;
    }

    public static boolean verifDate(String date) {
        // Vérifier format avec regex
        if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {  // AAAA-MM-JJ
            try {
                // verif date valide (évite 2024-02-30 par ex.)
                LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return true;
            } catch (DateTimeParseException e) {
                System.out.println("Date invalide : Date non existante");
                return false;
            }
        } else {
            System.out.println("Date invalide : Respectez AAAA-MM-JJ");
            return false;
        }
    }

    public static String demanderHeure(Scanner scanner) {
        String heure_entree = null;
        boolean heureValide = false;
        while (!heureValide) {
            System.out.print("Entrez l'heure (HH) : ");
            heure_entree = scanner.nextLine();
            heureValide = verifHeure(heure_entree);
        }
        return heure_entree;
    }

    public static boolean verifHeure(String heure) {
        if (heure.matches("\\d{2}")) {  // verif longueur + qu'on est bien 2 chiffres et pas de lettre sinon PareInt crash
            int heureInt = Integer.parseInt(heure);
            if (0 <= heureInt && heureInt <= 23) {
                return true;
            } else {
                System.out.println("Heure incorrecte : Entrez une heure valide (00-23).");
                return false;
            }
        } else {
            System.out.println("Heure incorrecte : Entrez une heure valide (00-23).");
            return false;
        }
    }

    public static int demanderTable(Scanner scanner) {
        int numtab_entree = -1;
        boolean numtabValide = false;
        while (!numtabValide) {
            System.out.print("Entrez le numéro de table : ");
            try {
                numtab_entree = scanner.nextInt();
                if (!verifTableExist(numtab_entree)) {
                    System.out.println("Table inexistante");
                } else {
                    numtabValide = true;
                }
            } catch (Exception e) {
                System.out.println("Numéro de table invalide : Entrez un nombre entier.");
                scanner.nextLine();  // vider le buffer
            }
        }
        return numtab_entree;
    }

    public static boolean verifTableExist(int numtab) {
        return numtab > 0 && Table.exist(numtab);
    }

    public static int demanderReservation(Scanner scanner) {
        int numres_entree = -1;
        boolean numresValide = false;
        while (!numresValide) {
            System.out.print("Entrez le numéro de réservation : ");
            try {
                numres_entree = scanner.nextInt();
                if (!Reservation.isExist((numres_entree))) {
                    System.out.println("Réservation inexistante");
                } else {
                    numresValide = true;
                }
            } catch (Exception e) {
                System.out.println("Numéro de réservation invalide : Entrez un nombre entier.");
                scanner.nextLine();  // vider le buffer
            }
        }
        return numres_entree;
    }

    *//**
         * Demande le plat et la quantité à commander
         *
         * @param scanner Scanner
         * @return [0] : le numéro du plat, [1] : la quantité
         *//*
    public static int[] demanderPlat(Scanner scanner) {
        int plat_entree = -1;
        int qte_entree = -1;
        boolean platValide = false;
        while (!platValide) {
            System.out.print("Entrez le numéro du plat : ");
            plat_entree = scanner.nextInt();
            System.out.print("Entrez la quantité à commander : ");
            qte_entree = scanner.nextInt();

            if (Plat.isExist(plat_entree)) {
                if (Plat.isDispo(plat_entree, qte_entree)) {
                    platValide = true;
                } else {
                    scanner.nextLine();  // vider le buffer
                    // print d'erreur gérer dans Plat.isDispo car + d'infos à la source
                }
            } else {
                scanner.nextLine();  // vider le buffer
                System.out.println("Plat inexistant");
            }
        }
        if (plat_entree == -1 || qte_entree == -1) {
            System.err.println("Erreur dans la demande du plat");
        }
        return new int[]{plat_entree, qte_entree};
    }*/
    }
}