@description('Location for the resources')
param location string

@description('Tags for the resources')
param tags object

@description('Name of the Azure OpenAI resource')
param name string

@description('Chat model deployment name')
param modelDeploymentName string

@description('Chat model version')
param modelVersion string

@description('Embedding model deployment name')
param embeddingDeploymentName string

@description('Embedding model version')
param embeddingVersion string

resource openAiAccount 'Microsoft.CognitiveServices/accounts@2024-10-01' = {
  name: name
  location: location
  tags: tags
  kind: 'OpenAI'
  sku: {
    name: 'S0'
  }
  properties: {
    customSubDomainName: name
    publicNetworkAccess: 'Enabled'
  }
}

resource chatModelDeployment 'Microsoft.CognitiveServices/accounts/deployments@2024-10-01' = {
  parent: openAiAccount
  name: modelDeploymentName
  sku: {
    name: 'GlobalStandard'
    capacity: 100
  }
  properties: {
    model: {
      format: 'OpenAI'
      name: 'gpt-5-mini'
      version: modelVersion
    }
    raiPolicyName: 'Microsoft.DefaultV2'
  }
}

resource embeddingModelDeployment 'Microsoft.CognitiveServices/accounts/deployments@2024-10-01' = {
  parent: openAiAccount
  name: embeddingDeploymentName
  sku: {
    name: 'Standard'
    capacity: 120
  }
  properties: {
    model: {
      format: 'OpenAI'
      name: 'text-embedding-3-small'
      version: embeddingVersion
    }
  }
  dependsOn: [
    chatModelDeployment
  ]
}

output endpoint string = openAiAccount.properties.endpoint
output apiKey string = openAiAccount.listKeys().key1
output name string = openAiAccount.name
