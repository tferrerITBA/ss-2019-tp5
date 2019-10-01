# ss-2019-tp5

## Compilación

Es necesario tener instalado Maven y Java 8. Comando:
```bash
mvn clean package # compilacion
java -jar target/tpes-1.0-SNAPSHOT.jar # ejecucion
```
También puede importarse el proyecto desde una IDE.

## Visualización

Asegurarse de correr el programa en modo `Single Run` y de que al finalizarse la ejecución se haya creado un archivo llamado `ovito_output.xyz`

Es necesario tener Ovito instalado.

Comandos:
```bash
ovito
# Desde ovito, en la barra de menús:
# Scripting > Run Script file... > render.py
```
*Nota*: para poder dar formato a los archivos importados con Ovito usando el script de python, es necesario editar el script con la ruta correcta a los archivos (no es necesario si se ejecuta ovito desde el directorio del proyecto)
