# PhotosBrindamour

Outil permettant de traiter les photos scolaires et de les préparer pour l'importation dans sytist.
On catégorise normalement les photo prises par date. Chaque date a son dossier qui contient le dossier timestamp, un
dossier Eleves avec toutes les photos d'élèves et un dossier "Noms" avec les photos du nom des élèves.

![img.png](img.png)

1. Pour chaque dossier de date
    1. Exécuter l'outil avec l'option -v.
    2. On obtient un dossion NamesValidation.
    3. On passe à travers chaque photo et on valide que le nom de l'élève dans la photo équivaut au nom de l'élève dans
       le nom de fichier.
    4. Si ça ne concorde pas:
        1. Il peut manquer un nom dans le fichier timestamp, on l'ajoute au bon endroit et on réessaie
        2. Il peut manquer une photo de nom dans "Noms". On peut prendre une photo de l'élève sans son nom et réessayer
    5. Quand tout concorde, on exécute avec l'option -e, on valide quelques fichiers pour voir s'il s'agit du bon élève
2. À la racine, où il y a le fichier timestamp "MASTER", créer un dossier "ToUpload", y mettre tous les dossiers
   contenus dans chaque dossier "Upload" de chaque dossier date, afin d'avoir un dossier contenant toutes les photos des
   élèves
3. À partir de la racine, exécuter l'option -p et valider que tous les élèves ont au moins 3 poses.
4. Si tout est ok, copier les dossiers "classe" dans des sous-dossiers sur ftp sous /sy-upload/
    5. Exporter pour les photos pour GPI avec l'option -s