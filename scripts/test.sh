
echo "🧪 Executando testes..."
echo ""

./mvnw clean test

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Todos os testes passaram!"
    echo ""
    echo "📊 Relatório de cobertura: target/site/jacoco/index.html"
else
    echo ""
    echo "❌ Alguns testes falharam. Verifique os logs acima."
    exit 1
fi