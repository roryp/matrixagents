@description('Location for the resources')
param location string

@description('Tags for the resources')
param tags object

@description('Name of the Container Registry')
param name string

resource containerRegistry 'Microsoft.ContainerRegistry/registries@2023-07-01' = {
  name: replace(name, '-', '')
  location: location
  tags: tags
  sku: {
    name: 'Basic'
  }
  properties: {
    adminUserEnabled: true
  }
}

output name string = containerRegistry.name
output loginServer string = containerRegistry.properties.loginServer
