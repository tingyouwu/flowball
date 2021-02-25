#import "TtchatFlowballPlugin.h"
#if __has_include(<ttchat_flowball/ttchat_flowball-Swift.h>)
#import <ttchat_flowball/ttchat_flowball-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "ttchat_flowball-Swift.h"
#endif

@implementation TtchatFlowballPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftTtchatFlowballPlugin registerWithRegistrar:registrar];
}
@end
