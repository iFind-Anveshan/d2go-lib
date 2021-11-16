#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint flutter_d2go.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'flutter_d2go'
  s.version          = '0.0.1'
  s.summary          = 'A new flutter plugin project.'
  s.description      = <<-DESC
A new flutter plugin project.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '12.0'

  # Flutter.framework does not contain a i386 slice.
  s.swift_version = '5.0'
  s.dependency "LibTorch", "~> 1.9.0"
  s.dependency "LibTorchvision", "0.10.0"
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386',
      'HEADER_SEARCH_PATHS' => '$(inherited) "${PODS_ROOT}/LibTorch/install/include"'
  }
 end
