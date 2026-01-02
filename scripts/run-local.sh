
echo "🚀 Iniciando aplicação cadastro-concurso..."
echo ""

# Verificar Java
if ! command -v java &> /dev/null; then
    echo "❌ Java não encontrado. Instale Java 17+: https://adoptium.net/"
    exit 1
fi

# Verificar versão do Java
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17+ é necessário. Versão atual: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java $JAVA_VERSION encontrado"
echo ""

# Criar diretório de uploads
mkdir -p uploads
echo "✅ Diretório de uploads criado"
echo ""

# Build e execução
echo "📦 Executando Maven build..."
./mvnw clean install -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build concluído com sucesso!"
    echo ""
    echo "🚀 Iniciando aplicação..."
    echo ""
    ./mvnw spring-boot:run
else
    echo ""
    echo "❌ Erro no build. Verifique os logs acima."
    exit 1
fi