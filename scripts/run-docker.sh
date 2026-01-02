
echo "🐳 Executando com Docker..."
echo ""

# Verificar Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker não encontrado. Instale: https://docs.docker.com/get-docker/"
    exit 1
fi

echo "✅ Docker encontrado"
echo ""

# Verificar docker-compose
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose não encontrado. Instale: https://docs.docker.com/compose/install/"
    exit 1
fi

echo "✅ docker-compose encontrado"
echo ""

# Build e execução
echo "📦 Construindo imagens Docker..."
docker-compose build

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build concluído!"
    echo ""
    echo "🚀 Iniciando containers..."
    docker-compose up -d
    echo ""
    echo "✅ Aplicação rodando!"
    echo ""
    echo "📍 URLs:"
    echo "   - App: http://localhost:8080"
    echo "   - PgAdmin: http://localhost:5050 (admin@admin.com / admin)"
    echo ""
    echo "📊 Logs: docker-compose logs -f app"
    echo "🛑 Parar: docker-compose down"
else
    echo ""
    echo "❌ Erro no build. Verifique os logs acima."
    exit 1
fi
